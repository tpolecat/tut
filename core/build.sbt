name := "tut-core"

libraryDependencies ++= Seq(
  scalaOrganization.value %  "scala-compiler" % scalaVersion.value
)

scalaVersion := "2.12.3"

crossScalaVersions := Seq("2.11.11", "2.12.3", "2.13.0-M1")

libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "1.0.5"

disablePlugins(ScriptedPlugin)

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
