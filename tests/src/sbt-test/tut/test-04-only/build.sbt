tutSettings

scalaVersion := sys.props("scala.version")

lazy val check = TaskKey[Unit]("check")

check := {
  val fs = (crossTarget.value / "tut").listFiles
  if (fs.exists(_.getName == "A.md"))
    error("Unexpected output file.")
  if (!fs.exists(_.getName == "B.md"))
    error("Expected output file is missing.")
}
