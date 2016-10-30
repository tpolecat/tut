val defaultScalaVersion = "2.11.8"

def testScalaVersion(logger: Logger): String = sys.env.get("TRAVIS_SCALA_VERSION").getOrElse {
  logger.warn(s"scripted tests: falling back to default Scala version $defaultScalaVersion")
  defaultScalaVersion
}

scriptedSettings

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

publishLocal := () // do tutPublishLocal at the top

// SBT 0.13.x plugin requires 2.10.x
scalaVersion := "2.10.6"
