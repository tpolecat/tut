tutSettings

scalaVersion := sys.props("scala.version")

lazy val check = TaskKey[Unit]("check")

check := {
  val expected = IO.readLines(file("expect.md"))
  val actual   = IO.readLines(crossTarget.value / "tut"/ "test.md")
  if (expected != actual)
    error("Output doesn't match expected: \n" + actual.mkString("\n"))
}
scalacOptions ++= Seq("-Xfatal-warnings", "-Ywarn-unused-import", "-feature")
