import sbtassembly.PathList

name := "EnronAnalysis"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies += "org.apache.spark" %% "spark-core" % "2.0.0"
// https://mvnrepository.com/artifact/org.apache.spark/spark-sql_2.11
libraryDependencies += "org.apache.spark" % "spark-sql_2.11" % "2.0.0"

libraryDependencies += "org.apache.tika" % "tika-core" % "1.13"
libraryDependencies += "org.apache.tika" % "tika-parsers" % "1.13"
libraryDependencies += "commons-codec" % "commons-codec" % "1.10"
// https://mvnrepository.com/artifact/com.amazonaws/aws-java-sdk-s3
libraryDependencies += "com.amazonaws" % "aws-java-sdk-s3" % "1.11.44"
// https://mvnrepository.com/artifact/org.jsoup/jsoup
libraryDependencies += "org.jsoup" % "jsoup" % "1.9.2"
// https://mvnrepository.com/artifact/com.databricks/spark-csv_2.11
libraryDependencies += "com.databricks" % "spark-csv_2.11" % "1.5.0"


assemblyMergeStrategy in assembly := {
  case PathList("org","aopalliance", xs @ _*) => MergeStrategy.last
  case PathList("javax", "inject", xs @ _*) => MergeStrategy.last
  case PathList("javax", "servlet", xs @ _*) => MergeStrategy.last
  case PathList("javax", "activation", xs @ _*) => MergeStrategy.last
  case PathList("org", "apache", xs @ _*) => MergeStrategy.last
  case PathList("com", "google", xs @ _*) => MergeStrategy.last
  case PathList("com", "esotericsoftware", xs @ _*) => MergeStrategy.last
  case PathList("com", "codahale", xs @ _*) => MergeStrategy.last
  case PathList("com", "yammer", xs @ _*) => MergeStrategy.last
  case PathList("org","apache","cxf",xs @ _*) =>MergeStrategy.discard
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case "about.html" => MergeStrategy.rename
  case "META-INF/ECLIPSEF.RSA" => MergeStrategy.last
  case "META-INF/mailcap" => MergeStrategy.last
  case "META-INF/mimetypes.default" => MergeStrategy.last
  case "plugin.properties" => MergeStrategy.last
  case "log4j.properties" => MergeStrategy.last
  case "bus-extensions.txt" =>MergeStrategy.discard

  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}