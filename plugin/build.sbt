import bintray.Keys._

name := "tut-plugin"

sbtPlugin := true

publishMavenStyle := false

bintrayPublishSettings

bintrayOrganization in bintray := Some("tpolecat")
