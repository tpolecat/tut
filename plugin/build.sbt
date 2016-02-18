import bintray.Keys._

name := "tut-plugin"

scalaVersion := "2.10.6"

sbtPlugin := true

publishMavenStyle := false

bintrayPublishSettings

bintrayOrganization in bintray := Some("tpolecat")

enablePlugins(BuildInfoPlugin)
buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion)
buildInfoPackage := "tut"
