name := "tut-core"

libraryDependencies ++= Seq(
  "org.scala-lang" %  "scala-compiler" % scalaVersion.value
)

scalaVersion := "2.11.4"

crossScalaVersions := Seq("2.10.4", "2.11.4")

libraryDependencies := {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, scalaMajor)) if scalaMajor >= 11 =>
      libraryDependencies.value :+ "org.scala-lang.modules" %% "scala-xml" % "1.0.1"
    case _ =>
      libraryDependencies.value
  }
}

bintrayPublishSettings
