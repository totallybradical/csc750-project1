name := """bingers-project1-server"""
organization := "com.bingers"

version := "1.0"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.12.4"

libraryDependencies += guice
libraryDependencies += javaJdbc
libraryDependencies += "org.xerial" % "sqlite-jdbc" % "3.8.6"
libraryDependencies += "com.fasterxml.jackson.core" % "jackson-core" % "2.8.7"