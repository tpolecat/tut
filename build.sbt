organization in ThisBuild := "org.tpolecat"

version in ThisBuild := "0.4.0-SNAPSHOT"

publishArtifact := false

lazy val core = project.in(file("core"))

lazy val plugin = project.in(file("plugin"))

lazy val example = project.in(file("example")).dependsOn(core)

bintrayPublishSettings

licenses in ThisBuild += ("MIT", url("http://opensource.org/licenses/MIT"))

