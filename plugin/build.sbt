import bintray.Keys._

name := "tut-plugin"

scalaVersion := "2.10.3"

sbtPlugin := true

publishMavenStyle := false

bintrayPublishSettings

bintrayOrganization in bintray := Some("tpolecat")
