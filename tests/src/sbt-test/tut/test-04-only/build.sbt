tutSettings

scalaVersion := sys.props("scala.version")

lazy val check = TaskKey[Unit]("check")

check := {
  val d = crossTarget.value / "tut"
  val expect = List(
    (false, d / "A.md"),
    (true,  d / "B.md"),
    (false, d / "sub1" / "C.md"),
    (true,  d / "sub1" / "sub1.1" / "D.md"),
    (true,  d / "sub1" / "sub1.1" / "E.md"),
    (true,  d / "sub2" / "F.md"),
    (false, d / "sub2" / "G.md")
  )
  val failed = expect.collect {
    case (b, f) if f.exists != b => (b, f)
  }
  if (failed.nonEmpty)
    error("Expectation failed for:" + failed.mkString("\n\t", "\n\t", ""))
}
