name := "tut-core"

libraryDependencies ++= Seq(
  scalaOrganization.value %  "scala-compiler" % scalaVersion.value
)

scalaVersion := "2.12.3"

crossScalaVersions := Seq("2.10.6", "2.11.10", scalaVersion.value)

libraryDependencies := {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, scalaMajor)) if scalaMajor >= 11 =>
      libraryDependencies.value :+ "org.scala-lang.modules" %% "scala-xml" % "1.0.5"
    case _ =>
      libraryDependencies.value
  }
}

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
