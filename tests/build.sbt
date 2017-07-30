val defaultScalaVersion = "2.11.10"

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

publishLocal := (()) // do tutPublishLocal at the top

scalaVersion := "2.12.3"
