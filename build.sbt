import sbt.Defaults.{runnerInit}
import sbt.Attributed.{data}

name := "tut"

version := "0.1"

scalaVersion := "2.10.1"

resolvers ++= Seq(
  "snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
  "releases"  at "http://oss.sonatype.org/content/repositories/releases",
  "tpolecat"  at "http://dl.bintray.com/tpolecat/maven"
)

libraryDependencies ++= Seq(
  "org.scalaz"     %% "scalaz-core"   % "7.0.4",
  "org.scalaz"     %% "scalaz-effect" % "7.0.4",
  "org.spire-math" %% "spire"         % "0.6.0",
  "org.tpolecat"   %% "atto"          % "0.1",
  "org.scala-lang" % "scala-compiler" % scalaVersion.value,
  "org.scala-lang" % "scala-reflect"  % scalaVersion.value
)

