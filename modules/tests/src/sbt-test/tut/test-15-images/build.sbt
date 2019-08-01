enablePlugins(TutPlugin)

scalaVersion := sys.props("scala.version")

resolvers += Resolver.bintrayRepo("cibotech", "public")
libraryDependencies += "com.cibo" %% "evilplot" % "0.4.0"

lazy val check = TaskKey[Unit]("check")

check := {
  val expected = IO.readLines(file("expect.md"))
  val actual   = IO.readLines(crossTarget.value / "tut"/ "test.md")
  if (expected != actual)
    sys.error("Output doesn't match expected: \n" + actual.mkString("\n"))
}
