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
  "-Dscala.version=" + scalaVersion.value
)

publishLocal := () // do tutPublishLocal at the top

scalaVersion := "2.11.8"
