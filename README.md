# spark-dynamodb-kinesis

#spark-submit --verbose --master yarn-cluster --num-executors <number> --executor-cores <number of cores> --executor-memory <memory> --jars awscala_2.10-0.5.3.jar,spark-streaming-kinesis-asl_2.10-1.5.2.jar,amazon-kinesis-client-1.3.0.jar,spark-streaming_2.10-1.5.2.jar --class "org.apache.spark.examples.streaming.KinesisDDB" "<jar location>" <kcl app name>" "<kinesis stream name>" "<kinesis stream aws endpoint" "<batch interval>"  > /tmp/kinesis.out &> /tmp/kinesis.out  2>&1
# eg.
#spark-submit --verbose --master yarn-cluster --num-executors 4 --executor-cores 10 --executor-memory 2g --jars awscala_2.10-0.5.3.jar,spark-streaming-kinesis-asl_2.10-1.5.2.jar,amazon-kinesis-client-1.3.0.jar,spark-streaming_2.10-1.5.2.jar --class "org.apache.spark.examples.streaming.KinesisDDB" /mnt/scala/spark-training/streaming/kinesis-scala-ddb/kinesis/target/scala-2.10/kinesisddb_2.10-1.0.jar "kinesis_twitter_kcl_20" "kinesis_twitter_20" "https://kinesis.ap-southeast-2.amazonaws.com" "5000"  > /tmp/kinesis.out &> /tmp/kinesis.out  2>&1

# Since this is a dirty hack, you will need to manually edit 2 things:
### DDB table name
https://github.com/fabiantan/spark-dynamodb-kinesis/blob/master/kinesis/src/main/scala/org/apache/spark/examples/streaming/KinesisDDB.scala#L111

### HDFS IP address
https://github.com/fabiantan/spark-dynamodb-kinesis/blob/master/kinesis/src/main/scala/org/apache/spark/examples/streaming/KinesisHelper.scala#L20

