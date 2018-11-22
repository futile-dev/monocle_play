name := "cats_playground"

version := "0.1"

//ensimeScalaVersion in ThisBuild := "2.12.6"

val monocleVersion = "1.5.0" // 1.5.0-cats based on cats 1.0.x

scalaVersion := "2.12.6"
scalacOptions += "-Ypartial-unification"
libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "1.2.0",
  "io.spray" %% "spray-json" % "1.3.4",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test",
  "com.github.julien-truffaut" %% "monocle-core" % monocleVersion,
  "com.github.julien-truffaut" %% "monocle-macro" % monocleVersion,
  "com.github.julien-truffaut" %% "monocle-law" % monocleVersion % "test"
)
