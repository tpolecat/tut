name := "tut-core"

libraryDependencies ++= Seq(
  scalaOrganization.value %  "scala-compiler" % scalaVersion.value
)

scalaVersion := "2.12.3"

crossScalaVersions := Seq("2.11.11", "2.12.3")

libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "1.0.5"

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
