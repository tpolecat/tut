enablePlugins(TutPlugin)

scalaVersion := sys.props("scala.version")

name := "foo"
version := "1.0.0"

lazy val check = TaskKey[Unit]("check")

check := {
  if(IO.readLines((artifactPath in makePom).value).exists(_.contains("tut-core")))
    sys.error(s"Found tut-core in the POM file")
}
