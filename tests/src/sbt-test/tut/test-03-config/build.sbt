tutSettings

scalaVersion := sys.props("scala.version")

lazy val check = TaskKey[Unit]("check")

check := {
  // verify that the compile classpath doesn't contain tut and thus
  // will not be part of the artifact's transitive dependencies.
  val ccp = (managedClasspath in Compile).value
  if (ccp.exists(_.data.getName.contains("tut-core")))
    error("Compile classpath contains tut-core.")
  // verify that the test classpath *does* contain tut
  val tcp = (managedClasspath in Test).value
  if (!tcp.exists(_.data.getName.contains("tut-core")))
    error("Test classpath doesn't contain tut-core.")
}
