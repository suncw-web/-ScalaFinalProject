
ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.12.11"

lazy val root = (project in file("."))
  .settings(
    name := "TradingApp",
  )

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.2.16" % Test,// Testing


  "com.typesafe.akka" %% "akka-http" % "10.2.10",       // For HTTP server, if needed
  "com.typesafe.akka" %% "akka-actor" % "2.6.17",         // For Akka actors, if needed
  "com.typesafe.akka" %% "akka-stream" % "2.6.17",         // For Akka stream, if needed

  // Circe for JSON parsing
  "io.circe" %% "circe-core" % "0.14.3",  // Circe core
  "io.circe" %% "circe-generic" % "0.14.3", // Circe for automatic derivation of encoders/decoders
  "io.circe" %% "circe-parser" % "0.14.3",  // Circe parser


  //HTTP client library
  "org.scalaj" %% "scalaj-http" % "2.4.2"
)


