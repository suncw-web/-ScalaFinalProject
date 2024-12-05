ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.12.11"

lazy val root = (project in file("."))
  .settings(
    name := "TradingApp",
  )

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.2.16" % Test, // Testing
  "org.mockito" %% "mockito-scala" % "1.17.27" % Test,
//
  "com.typesafe.akka" %% "akka-http" % "10.2.10",   // For HTTP server
  "com.typesafe.akka" %% "akka-actor" % "2.8.6",    // For Akka actors
  "com.typesafe.akka" %% "akka-actor-typed" % "2.8.6",
  "com.typesafe.akka" %% "akka-stream" % "2.8.6",    // For Akka stream
//
//  // Circe for JSON parsing
  "io.circe" %% "circe-core" % "0.14.5", // Circe core
  "io.circe" %% "circe-generic" % "0.14.5", // Circe for automatic derivation of encoders/decoders
  "io.circe" %% "circe-parser" % "0.14.5", // Circe parser

//  "org.json4s" %% "json4s-native" % "4.0.3",
//  "org.json4s" % "json4s-core_2.12" % "3.6.11",
//  "org.json4s" % "json4s-ext_2.12" % "3.6.11",

  "io.spray" %% "spray-json" % "1.3.6", // Ensure you are using the correct version here

//  // HTTP client library
  "org.scalaj" %% "scalaj-http" % "2.4.2",

  // Spark core and machine learning
  "org.apache.spark" %% "spark-core" % "3.5.1",
  "org.apache.spark" %% "spark-sql" % "3.5.1",
  "org.apache.spark" %% "spark-mllib" % "3.5.1",

  //Kantan.csv
  "com.nrinaudo" %% "kantan.csv" % "0.6.1"
)

val sprayGroup = "io.spray"
val sprayJsonVersion = "1.3.6"
libraryDependencies ++= List("spray-json") map {c => sprayGroup %% c %
  sprayJsonVersion}