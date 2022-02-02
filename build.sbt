name := """app-demo"""
organization := "io.bokun"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava, PlayEbean)

scalaVersion := "2.13.1"

libraryDependencies += guice
libraryDependencies ++= Seq(
  javaWs, javaJdbc
)

libraryDependencies += "com.h2database" % "h2" % "1.4.192"
libraryDependencies += "commons-codec" % "commons-codec" % "1.14"

PlayKeys.devSettings += "play.server.http.port" -> "8181"
PlayKeys.fileWatchService := play.dev.filewatch.FileWatchService.jdk7(play.sbt.run.toLoggerProxy(sLog.value))
