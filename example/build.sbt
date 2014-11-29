import sbt._
import sbt.Keys._
import sbt.Defaults.runnerInit
import sbt.Attributed.data

name := "tut-example"

resolvers += "tpolecat"  at "http://dl.bintray.com/tpolecat/maven"

libraryDependencies ++= Seq(
  "org.tpolecat" %% "atto-core"  % "0.4.1", // Core parsers and combinators
  "org.tpolecat" %% "atto-spire" % "0.4.1"  // Optional, parsers for unsigned integral types
)

scalaVersion := "2.11.4"

publishArtifact := false

// N.B. the stuff below usually comes for free via `tutSettings` but I don't know how to make that 
// work so I'm doing it like this.

lazy val tut = TaskKey[Unit]("tut", "create tut documentation")

lazy val tutSourceDirectory = SettingKey[File]("tutSourceDirectory", "where to look for tut sources")

tutSourceDirectory := sourceDirectory.value / "main" / "tut"

tut := {
  val r   = (runner in (Compile, doc)).value
  val in  = tutSourceDirectory.value
  val out = crossTarget.value / "tut"
  val cp  = (fullClasspath in (Compile, doc)).value
  toError(r.run("tut.TutMain", 
                data(cp), 
                Seq(in.getAbsolutePath, out.getAbsolutePath), 
                streams.value.log))
}
