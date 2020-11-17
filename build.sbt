name := "akka-http-basic-api"

version := "0.1"

scalaVersion := "2.13.3"

val akkaVersion = "2.6.10"
val akkaHttpVersion = "10.2.1"
val scalaTestVersion = "3.2.2"

libraryDependencies ++= Seq(
  // akka streams
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  // akka http
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion,
  // testing
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "org.scalatest" %% "scalatest" % scalaTestVersion,
  "com.datastax.cassandra" % "cassandra-driver-core" % "3.10.0",

)
