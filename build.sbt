import ReleaseTransformations._

lazy val `2.10` = "2.10.6"
lazy val `2.12` = "2.12.4"
lazy val `2.11` = "2.11.11"
lazy val `2.13` = "2.13.0-M1"

lazy val commonSettings =
  Seq(
    organization := "org.tpolecat",
    scalaVersion := `2.12`,
    licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
    resolvers += Resolver.typesafeIvyRepo("releases"),
    releaseProcess := Nil
  )

lazy val noPublishSettings =
  commonSettings ++ Seq(
    publishLocal := (()),
    publish := (()),
  )

lazy val publishSettings =
  commonSettings ++ Seq(
    bintrayOrganization := Some("tpolecat"),
    publishMavenStyle := true,
    publishArtifact in Test := false,
    homepage := Some(url("https://github.com/tpolecat/tut")),
    pomIncludeRepository := Function.const(false),
    pomExtra := (
      <developers>
        <developer>
          <id>tpolecat</id>
          <name>Rob Norris</name>
          <url>http://tpolecat.org</url>
        </developer>
      </developers>
    ),
  )

lazy val root = project
  .in(file("."))
  .settings(noPublishSettings)
  .dependsOn(core, plugin, tests)
  .aggregate(core, plugin, tests)
  .settings(
    releaseCrossBuild := true,
    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      runClean,
      releaseStepCommand("tests/scripted"),
      setReleaseVersion,
      commitReleaseVersion,
      tagRelease,
      publishArtifacts,
      setNextVersion,
      commitNextVersion,
      pushChanges
    )
  )

lazy val core = project
  .in(file("core"))
  .settings(publishSettings)
  .settings(
    name := "tut-core",
    libraryDependencies += scalaOrganization.value % "scala-compiler" % scalaVersion.value,
    crossScalaVersions := Seq(`2.10`, `2.11`, `2.12`, `2.13`),
    libraryDependencies := {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, scalaMajor)) if scalaMajor >= 11 =>
          libraryDependencies.value :+ "org.scala-lang.modules" %% "scala-xml" % "1.0.6"
        case _ =>
          libraryDependencies.value
      }
    },
    // scripted-plugin is enabled by default, in particular in this non-sbt subproject
    // this means that switching to 2.11.11 will result in non-existent dependencies
    libraryDependencies ~= { _.filterNot(_.organization == "org.scala-sbt") },
    scalacOptions ++= Seq(
      "-deprecation",
      "-encoding", "UTF-8",
      "-feature",
      "-language:existentials",
      "-language:higherKinds",
      "-language:implicitConversions",
      "-unchecked",
      "-Xfatal-warnings",
      "-Xlint",
      "-Yno-adapted-args",
      "-Ywarn-dead-code",
      "-Ywarn-numeric-widen",
      "-Ywarn-value-discard",
      "-Xfuture"
    )
  )

lazy val plugin = project
  .in(file("plugin"))
  .settings(publishSettings)
  .settings(
    name := "tut-plugin",
    scalaVersion := `2.12`,
    sbtPlugin := true,
    publishMavenStyle := false,
  )
  .enablePlugins(BuildInfoPlugin)
  .settings(
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "tut"
  )

lazy val tests = project
  .in(file("tests"))
  .settings(noPublishSettings)
  .settings(
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
      "-Dscala.version=" + `2.11`
    ),
  )
