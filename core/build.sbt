name := "tut-core"

libraryDependencies ++= Seq(
  scalaOrganization.value %  "scala-compiler" % scalaVersion.value
)

scalaVersion := "2.12.3"

crossScalaVersions := Seq("2.10.6", "2.11.11", "2.12.3", "2.13.0-M1")

libraryDependencies := {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, scalaMajor)) if scalaMajor >= 11 =>
      libraryDependencies.value :+ "org.scala-lang.modules" %% "scala-xml" % "1.0.6"
    case _ =>
      libraryDependencies.value
  }
}

// scripted-plugin is enabled by default, in particular in this non-sbt subproject
// this means that switching to 2.11.11 will result in non-existent dependencies
libraryDependencies ~= { _.filterNot(_.organization == "org.scala-sbt") }

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Xfuture"
)
