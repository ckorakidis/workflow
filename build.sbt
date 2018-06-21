name := "workflow"

version := "0.1"

scalaVersion := "2.12.4"

lazy val finchVersion = "0.21.0"
lazy val circeVersion = "0.10.0-M1"
lazy val twitterServerVersion = "18.6.0"
lazy val finagleVersion = "18.6.0"

lazy val scalaTestVersion = "3.0.1"
lazy val mockitoTestVersion = "1.10.19"

libraryDependencies ++= Seq(
  "com.github.finagle" %% "finch-core" % finchVersion,
  "com.github.finagle" %% "finch-circe" % finchVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "io.circe" %% "circe-generic-extras" % circeVersion,
  "com.twitter" %% "twitter-server" % twitterServerVersion,
  "com.twitter" %% "finagle-stats" % finagleVersion,

  "org.scalatest" %% "scalatest" % scalaTestVersion % "test",
  "org.mockito" % "mockito-all" % mockitoTestVersion % "test",
  "net.dericbourg.daily-utils" %% "twitter-utils" % "0.1.8",
  "net.dericbourg.daily-utils" %% "twitter-test-future" % "0.1.8" % Test
)