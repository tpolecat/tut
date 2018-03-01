import ReleaseTransformations._
import microsites._

lazy val `2.10` = "2.10.6"
lazy val `2.12` = "2.12.4"
lazy val `2.11` = "2.11.12"
lazy val `2.13` = "2.13.0-M3"

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
    // releaseCrossBuild := true,
    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      runClean,
      releaseStepCommand("tests/scripted"),
      setReleaseVersion,
      commitReleaseVersion,
      tagRelease,
      // publishArtifacts, // doesn't work, rats
      releaseStepCommandAndRemaining("+publish"),
      setNextVersion,
      commitNextVersion,
      pushChanges
    )
  )

lazy val core = project
  .in(file("modules/core"))
  .settings(publishSettings)
  .settings(
    Seq(Compile, Test).map { sc =>
      (unmanagedSourceDirectories in sc) ++= {
        (unmanagedSourceDirectories in sc ).value.map { dir: File =>
          CrossVersion.partialVersion(scalaVersion.value) match {
            case Some((2, y)) if y <= 12 => new File(dir.getPath + "-2.12-")
            case Some((2, y))            => new File(dir.getPath + "-2.13+")
          }
        }
      }
    }
  )
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
  .in(file("modules/plugin"))
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
  .in(file("modules/tests"))
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

lazy val docs = project
  .in(file("modules/docs"))
  .dependsOn(core)
  .enablePlugins(MicrositesPlugin)
  .settings(commonSettings)
  .settings(noPublishSettings)
  .settings(
    scalacOptions in (Compile, console) += "-Xfatal-warnings", // turn this back on for tut
    libraryDependencies ++= Seq(
      // doc dependencies here
    ),
    fork in Test := true,
    // Settings for sbt-microsites https://47deg.github.io/sbt-microsites/
    micrositeImgDirectory     := baseDirectory.value / "src/main/resources/microsite/img",
    micrositeName             := "tut",
    micrositeDescription      := "A tutorial generator for Scala.",
    micrositeAuthor           := "Rob Norris",
    micrositeGithubOwner      := "tpolecat",
    micrositeGithubRepo       := "tut",
    micrositeGitterChannel    := false, // no me gusta
    micrositeBaseUrl          := "/tut/",
    // micrositeDocumentationUrl := "https://www.javadoc.io/doc/org.tpolecat/tut-core_2.12",
    micrositeHighlightTheme   := "color-brewer",
    micrositePalette := micrositePalette.value ++ Map(
      "brand-primary"     -> "#A07138",
      "brand-secondary"   -> "#A07138",
      "brand-tertiary"    -> "#A07138",
      // "gray-dark"         -> "#453E46",
      // "gray"              -> "#837F84",
      // "gray-light"        -> "#E3E2E3",
      // "gray-lighter"      -> "#F4F3F4",
      // "white-color"       -> "#FFFFFF"
    ),
    micrositeConfigYaml := ConfigYml(
      yamlCustomProperties = Map(
        "tutVersion"     -> version.value,
        "scalaVersions"  -> (crossScalaVersions in core).value.map(CrossVersion.partialVersion).flatten.map(_._2).mkString("2.", "/", ""), // 2.11/12
        "scala213"       -> `2.13`
      )
    ),
    micrositeExtraMdFiles := Map(
      file("CHANGELOG.md") -> ExtraMdFileConfig("changelog.md", "page", Map("title" -> "Changelog", "section" -> "Changelog", "position" -> "100")),
      file("LICENSE")      -> ExtraMdFileConfig("license.md",   "page", Map("title" -> "License",   "section" -> "License",   "position" -> "101"))
    ),
    tutNameFilter := "xyz".r // don't run tut on anything (ironic eh?)
  )
