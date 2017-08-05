name := "tut-plugin"

scalaVersion := "2.12.3"

crossScalaVersions := Seq("2.12.3")

sbtPlugin := true

publishMavenStyle := false

bintrayOrganization := Some("tpolecat")

enablePlugins(BuildInfoPlugin)
buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion)
buildInfoPackage := "tut"

// tests

val defaultScalaVersion = "2.11.11"

def testScalaVersion(logger: Logger): String = sys.env.get("TRAVIS_SCALA_VERSION").getOrElse {
  logger.warn(s"scripted tests: falling back to default Scala version $defaultScalaVersion")
  defaultScalaVersion
}

scriptedLaunchOpts ++= Seq(
  "-Dfile.encoding=UTF-8",
  "-XX:MaxPermSize=1024m",
  "-Xms512m",
  "-Xmx3500m", 
  "-Xss2m",
  "-XX:ReservedCodeCacheSize=256m",
  "-XX:+TieredCompilation",
  "-XX:+CMSClassUnloadingEnabled",
  "-XX:+UseConcMarkSweepGC",
  "-Dproject.version=" + version.value,
  "-Dscala.version=" + testScalaVersion(sLog.value)
)
