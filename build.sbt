name := "newages.wallet"
version := "0.1"
scalaVersion := "2.13.5"

parallelExecution in Test := false

val akkaV = "2.6.13"
val akkaHttpV = "10.2.4"
val circeV = "0.12.3"
val scalasticV = "3.2.5"
val catsV = "2.2.0"
val sttpV = "3.1.9"
val dockerJavaV = "3.2.7"
val doobieV = "0.12.1"
val mockitoV = "1.16.32"
val http4sV = "0.21.21"

libraryDependencies ++= Seq(
  // logger
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  // cats
  "org.typelevel" %% "cats-core" % catsV,
  "org.typelevel" %% "cats-effect" % catsV,
  // Circe
  "io.circe" %% "circe-core" % circeV,
  "io.circe" %% "circe-generic" % circeV,
  "io.circe" %% "circe-parser" % circeV,
  "io.circe" %% "circe-literal" % circeV,
  // Doobie / PostgreSQL
  "org.tpolecat" %% "doobie-core" % doobieV,
  "org.tpolecat" %% "doobie-postgres" % doobieV,
  "org.tpolecat" %% "doobie-postgres-circe" % doobieV,
  "org.tpolecat" %% "doobie-hikari" % doobieV,
  "org.tpolecat" %% "doobie-quill" % doobieV,
  // http4s
  "org.http4s" %% "http4s-dsl" % http4sV,
  "org.http4s" %% "http4s-blaze-server" % http4sV,
  "org.http4s" %% "http4s-blaze-client" % http4sV,
  "org.http4s" %% "http4s-circe" % http4sV,
  // Testing
  "org.scalactic" %% "scalactic" % scalasticV % Test,
  "org.scalatest" %% "scalatest" % scalasticV % Test,
  "org.tpolecat" %% "doobie-scalatest" % doobieV % Test,
  "org.mockito" %% "mockito-scala" % mockitoV % Test,
  "org.mockito" %% "mockito-scala-scalatest" % mockitoV % Test,
  "org.mockito" %% "mockito-scala-cats" % mockitoV % Test,
  // docker
  "com.github.docker-java" % "docker-java" % dockerJavaV,
  "com.github.docker-java" % "docker-java-transport-httpclient5" % dockerJavaV,
  // somonr-utils
  "io.simonr" %% "util-docker" % "0.1.5-SNAPSHOT"
)

scalacOptions ++= Seq(
  "-encoding",
  "utf8",
  "-deprecation",
  "-unchecked",
  "-feature",
  "-language:implicitConversions"
)

resolvers += "Artifactory" at "https://simonr.jfrog.io/artifactory/sbt-proxy/"
