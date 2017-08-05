enablePlugins(TutPlugin)

scalaVersion := sys.props("scala.version")

lazy val check = TaskKey[Unit]("check")

check := {
  // runs tut command and generates output files as a side effect
  val tutOutput = tut.value
  if (!tutOutput.exists(_._2 == "A.md")) sys.error("A.md not in output")
  if (!tutOutput.exists(_._2 == "B.md")) sys.error("B.md not in output")
  if (!tutOutput.exists(_._2 == "sub/D.md")) sys.error("sub/D.md not in output")
  val fs = (crossTarget.value / "tut")
  if (!(fs / "A.md").exists) sys.error("A.md doesn't exist")
  if (!(fs / "B.md").exists) sys.error("B.md doesn't exist")
  if (!(fs / "sub" / "D.md").exists) sys.error("sub/D.md doesn't exist")
  val expected = IO.readLines(tutSourceDirectory.value / "sub" / "E.ignore")
  val actual   = IO.readLines(tutTargetDirectory.value / "sub" / "E.ignore")
  if (expected != actual)
    sys.error("sub/E.ignored wasn't copied properly: \n" + actual.mkString("\n"))
}
