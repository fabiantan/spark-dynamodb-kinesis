package org.apache.spark.examples.streaming

import java.nio.ByteBuffer

import scala.util.Random

import com.amazonaws.auth.{DefaultAWSCredentialsProviderChain, BasicAWSCredentials}
import com.amazonaws.regions.RegionUtils
import com.amazonaws.services.kinesis.AmazonKinesisClient
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.InitialPositionInStream
import com.amazonaws.services.kinesis.model.PutRecordRequest
import org.apache.log4j.{Level, Logger}

import org.apache.spark.{Logging, SparkConf}
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.{Milliseconds, StreamingContext}
import org.apache.spark.streaming.dstream.DStream.toPairDStreamFunctions
import org.apache.spark.streaming.kinesis.KinesisUtils

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.{Time, Seconds, StreamingContext}
import org.apache.spark.util.IntParam
import org.apache.spark.util.{MutableURLClassLoader, Clock, ManualClock, Utils}
import KinesisHelper._

import org.apache.spark.storage.StorageLevel
// Adding DDB support
import awscala._, dynamodbv2._


object KinesisDDB extends Logging {
  def main(args: Array[String]) {
    // Check that all required args were passed in.
    if (args.length != 4) {
      System.err.println(
        """
          |Usage: KinesisDDB <app-name> <stream-name> <endpoint-url> <batch-length-ms>
          |
          |    <app-name> is the name of the consumer app, used to track the read data in DynamoDB
          |    <stream-name> is the name of the Kinesis stream
          |    <endpoint-url> is the endpoint of the Kinesis service
          |                   (e.g. https://kinesis.us-east-1.amazonaws.com)
          |    <batch-length-ms> How long is each batch for the temp table view directly from stream in milliseconds
        """.stripMargin)
      System.exit(1)
    }

    //StreamingExamples.setStreamingLogLevels()

    // Populate the appropriate variables from the given args
    val Array(appName, streamName, endpointUrl, batchLength ) = args

    val checkpointDir = KinesisHelper.getCheckpointDirectory()


    // Determine the number of shards from the stream using the low-level Kinesis Client
    // from the AWS Java SDK.
    val credentials = new DefaultAWSCredentialsProviderChain().getCredentials()
    require(credentials != null,
      "No AWS credentials found. Please specify credentials using one of the methods specified " +
        "in http://docs.aws.amazon.com/AWSSdkDocsJava/latest/DeveloperGuide/credentials.html")
    val kinesisClient = new AmazonKinesisClient(credentials)
    kinesisClient.setEndpoint(endpointUrl)
    val numShards = kinesisClient.describeStream(streamName).getStreamDescription().getShards().size


    // In this example, we're going to create 1 Kinesis Receiver/input DStream for each shard.
    // This is not a necessity; if there are less receivers/DStreams than the number of shards,
    // then the shards will be automatically distributed among the receivers and each receiver
    // will receive data from multiple shards.
    val numStreams = numShards

    // Spark Streaming batch interval
    val batchInterval = Milliseconds(batchLength.toLong)

    // Kinesis checkpoint interval is the interval at which the DynamoDB is updated with information
    // on sequence number of records that have been received. Same as batchInterval for this
    // example.
    val kinesisCheckpointInterval = batchInterval

    // Get the region name from the endpoint URL to save Kinesis Client Library metadata in
    // DynamoDB of the same region as the Kinesis stream
    val regionName = RegionUtils.getRegionByEndpoint(endpointUrl).getName()


    // Setup the SparkConfig and StreamingContext
    val sparkConfig = new SparkConf().setAppName("KinesisDDB").set("spark.streaming.receiver.writeAheadLog.enable","true")
    val ssc = new StreamingContext(sparkConfig, batchInterval)

    // Create the Kinesis DStreams
    val kinesisStreams = (0 until numStreams).map { i =>
      KinesisUtils.createStream(ssc, appName, streamName, endpointUrl, regionName,
        InitialPositionInStream.TRIM_HORIZON, kinesisCheckpointInterval, StorageLevel.MEMORY_AND_DISK_2)
    }

    // Union all the streams
    val unionStreams = ssc.union(kinesisStreams)

    // Convert datatype to string
    val words2 = unionStreams.flatMap(byteArray => new String(byteArray).split(" "))

    // Map each word to a (word, 1) tuple so we can reduce by key to count the words
    val DStream = words2.map(word => (word, 1)).reduceByKey(_ + _)

    
   DStream.foreachRDD(rdd => {
      rdd.foreachPartition(partitionOfRecords => {
			implicit val dynamoDB = DynamoDB.at(Region.Sydney)
    			val table: Table = dynamoDB.table("ddbscala").get
          		
			partitionOfRecords.foreach(record => {
          		//println(record)
			table.put(record._1, "Count" -> record._2) 
		})
      })
   })

    ssc.checkpoint(checkpointDir)
   
    ssc.start()
    ssc.awaitTermination()
  }
}

