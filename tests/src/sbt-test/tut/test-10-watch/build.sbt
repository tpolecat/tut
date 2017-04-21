tutSettings

scalaVersion := sys.props("scala.version")

lazy val check = TaskKey[Unit]("check")

check := {
  val sources = watchSources.value

  // Makes sure files that match the default pattern are watched.
  List("test.htm", "test.html", "test.markdown", "test.md", "test.txt").foreach { name =>
    if(!sources.exists(_.getName() == name))
    error(s"Failed to find $name in watched sources")
  }

  // Makes sure that files that don't aren't watched.
  if(sources.exists(_.getName() == "test.xml"))
    error("Found test.xml in watched sources")
}
