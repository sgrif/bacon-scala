// Set the project name
name := "Scala Bacon"

autoCompilerPlugins := true

addCompilerPlugin("org.scala-lang.plugins" % "continuations" % "2.10.3")

organization := "com.seantheprogrammer"

// Set the project version
version := "0.1.2"

// Set the version of Scala to use
scalaVersion := "2.10.3"

// Make the Java compiler work better with Android apps
javacOptions ++= Seq("-encoding", "UTF-8", "-source", "1.6", "-target", "1.6")

scalacOptions ++= Seq("-deprecation", "-feature", "-P:continuations:enable")

// Add ScalaTest
libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "1.9.1" % "test"
)
