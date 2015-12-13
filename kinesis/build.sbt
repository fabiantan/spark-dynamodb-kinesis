name := "KinesisDDB"

version := "1.0"

scalaVersion := "2.10.5"

libraryDependencies += "org.apache.spark" % "spark-streaming_2.10" % "1.5.2"
libraryDependencies += "org.apache.spark" % "spark-streaming-kinesis-asl_2.10" % "1.5.2"
libraryDependencies += "com.github.seratch" %% "awscala" % "0.5.3"

resolvers += "Akka Repository" at "http://repo.akka.io/releases/"

// META-INF discarding
mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) =>
  {
    case PathList("META-INF", xs @ _*) => MergeStrategy.discard
    case x => MergeStrategy.first
   }
}
