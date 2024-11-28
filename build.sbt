ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.12.11"

lazy val root = (project in file("."))
  .settings(
    name := "TradingApp",
  )

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.2.16" % Test, // Testing
//
  "com.typesafe.akka" %% "akka-http" % "10.2.10",   // For HTTP server
  "com.typesafe.akka" %% "akka-actor" % "2.6.17",    // For Akka actors
  "com.typesafe.akka" %% "akka-stream" % "2.8.6",    // For Akka stream
//
//  // Circe for JSON parsing
  "io.circe" %% "circe-core" % "0.14.3", // Circe core
  "io.circe" %% "circe-generic" % "0.14.3", // Circe for automatic derivation of encoders/decoders
  "io.circe" %% "circe-parser" % "0.14.3", // Circe parser
//
//  // HTTP client library
  "org.scalaj" %% "scalaj-http" % "2.4.2",

  // Spark core and machine learning
  "org.apache.spark" %% "spark-core" % "3.5.1",
  "org.apache.spark" %% "spark-sql" % "3.5.1",
  "org.apache.spark" %% "spark-mllib" % "3.5.1"
)