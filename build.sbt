organization := "org.ergoplatform"
name := "ergo-connect"
version := "1.9.1"
scalaVersion := "2.12.8"

resolvers ++= Seq(
  "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/",
  "SonaType" at "https://oss.sonatype.org/content/groups/public",
  "Typesafe maven releases" at "http://repo.typesafe.com/typesafe/maven-releases/",
)

homepage := Some(url("http://ergoplatform.org/"))
licenses := Seq("CC0" -> url("https://creativecommons.org/publicdomain/zero/1.0/legalcode"))

val scorexVersion = "2.0.0-RC3"

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-async" % "0.9.7",

  "javax.xml.bind" % "jaxb-api" % "2.+",
  
  ("org.scorexfoundation" %% "scorex-core" % scorexVersion).exclude("ch.qos.logback", "logback-classic"),
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",

  "com.typesafe.akka" %% "akka-testkit" % "2.5.+" % "test",
  "com.typesafe.akka" %% "akka-http-testkit" % "10.+" % "test",
  "org.asynchttpclient" % "async-http-client" % "2.6.+",
  "io.netty" % "netty" % "3.10.6.Final",
  "com.fasterxml.jackson.dataformat" % "jackson-dataformat-properties" % "2.9.2" % "test",
)

fork := true

scalacOptions ++= Seq("-Xfatal-warnings", "-feature", "-deprecation")