name := "scalac"
version := "0.1"
scalaVersion := "2.13.5"

val akkaV = "2.6.13"
val akkaHttpV = "10.2.4"
val circeV = "0.12.3"
val scalasticV = "3.2.5"
val catsV = "2.2.0"
val sttpV = "3.1.9"

libraryDependencies ++= Seq(
  // logger
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  // sttp
  "com.softwaremill.sttp.client3" %% "core" % sttpV,
  "com.softwaremill.sttp.client3" %% "async-http-client-backend-fs2" % sttpV,
  // Akka
  "com.typesafe.akka" %% "akka-slf4j" % akkaV,
  "com.typesafe.akka" %% "akka-actor-typed" % akkaV,
  "com.typesafe.akka" %% "akka-stream-typed" % akkaV,
  "com.typesafe.akka" %% "akka-http" % akkaHttpV,
  "com.typesafe.akka" %% "akka-http-core" % akkaHttpV,
  // cats
  "org.typelevel" %% "cats-core" % catsV,
  "org.typelevel" %% "cats-effect" % catsV,
  // Circe
  "io.circe" %% "circe-core" % circeV,
  "io.circe" %% "circe-generic" % circeV,
  "io.circe" %% "circe-parser" % circeV,
  // Testing
  "org.scalactic" %% "scalactic" % scalasticV,
  "org.scalatest" %% "scalatest" % scalasticV % Test,
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaV,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpV % Test
)

scalacOptions ++= Seq(
  "-encoding",
  "utf8",
  "-deprecation",
  "-unchecked",
  "-feature",
  "-language:implicitConversions"
)
