organization in ThisBuild := "org.tpolecat"

version in ThisBuild := "0.2-SNAPSHOT"

scalaVersion in ThisBuild := "2.10.3"

publishArtifact := false

lazy val core = project.in(file("core"))

lazy val plugin = project.in(file("plugin"))

bintrayPublishSettings

licenses in ThisBuild += ("MIT", url("http://opensource.org/licenses/MIT"))

