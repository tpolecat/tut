tutSettings

scalaVersion := sys.props("scala.version")

lazy val check = TaskKey[Unit]("check")

check := {
  val fs = (crossTarget.value / "tut")
  if (!(fs / "A.md").exists) error("A.md doesn't exist")
  if (!(fs / "B.md").exists) error("B.md doesn't exist")
  if (!(fs / "sub" / "D.md").exists) error("sub/D.md doesn't exist")
  val expected = IO.readLines(tutSourceDirectory.value / "sub" / "E.ignore")
  val actual   = IO.readLines(tutTargetDirectory.value / "sub" / "E.ignore")
  if (expected != actual) 
    error("sub/E.ignored wasn't copied properly: \n" + actual.mkString("\n"))
}
