name := "tut-core"

libraryDependencies ++= Seq(
  "org.scala-lang" %  "scala-compiler" % scalaVersion.value
)

scalaVersion := "2.11.8"

crossScalaVersions := Seq("2.10.6", scalaVersion.value, "2.12.0-M4", "2.12.0-M5", "2.12.0-RC1")

libraryDependencies := {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, scalaMajor)) if scalaMajor >= 11 =>
      libraryDependencies.value :+ "org.scala-lang.modules" %% "scala-xml" % "1.0.5"
    case _ =>
      libraryDependencies.value
  }
}

