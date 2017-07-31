enablePlugins(TutPlugin)

scalaVersion := sys.props("scala.version")

lazy val check = TaskKey[Unit]("check")

check := {
  val expected = IO.readLines(file("expect.md"))
  val actual   = IO.readLines(crossTarget.value / "tut"/ "test.md")
  if (expected != actual)
    sys.error("Output doesn't match expected: \n" + actual.mkString("\n"))
}

scalacOptions ++= Seq("-language:higherKinds", "-Xfatal-warnings")
scalacOptions in Tut += "-language:existentials"
