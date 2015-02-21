tutSettings

scalaVersion := "2.11.4"

lazy val check = TaskKey[Unit]("check")

check := {
  val expected = IO.readLines(file("expect.md"))
  val actual   = IO.readLines(file("target/scala-2.11/tut/test.md"))
  if (expected != actual) 
    error("Output doesn't match expected: \n" + actual.mkString("\n"))
}

resolvers += "bintray/non" at "http://dl.bintray.com/non/maven"

addCompilerPlugin("org.spire-math" % "kind-projector_2.11" % "0.5.2")
