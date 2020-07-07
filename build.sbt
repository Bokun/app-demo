name := """app-demo"""
organization := "io.bokun"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.13.1"

libraryDependencies += guice
libraryDependencies ++= Seq(
  javaWs
)

PlayKeys.devSettings += "play.server.http.port" -> "8181"
