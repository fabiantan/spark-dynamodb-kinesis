package org.apache.spark.examples.streaming

import org.apache.spark.streaming._
import org.apache.spark.storage.StorageLevel
import scala.io.Source
import scala.collection.mutable.HashMap
import java.io.File
//import org.apache.log4j.Logger
//import org.apache.log4j.Level
import sys.process.stringSeqToProcess

object KinesisHelper {
  //Logger.getLogger("org.apache.spark").setLevel(Level.WARN)
  //Logger.getLogger("org.apache.spark.storage.BlockManager").setLevel(Level.ERROR)
    
  /** Returns the HDFS URL */
  def getCheckpointDirectory(): String = {
    try {
      //val name : String = Seq("bash", "-c", "curl -s http://169.254.169.254/latest/meta-data/hostname") !! ;
      val name : String = "172.31.30.248" //Hardcode the NN IP address
      println("Hostname = " + name)
      "hdfs://" + name.trim + ":8020/checkpoint/"
    } catch {
      case e: Exception => {
        "./checkpoint/"
      }
    }
  }
}

