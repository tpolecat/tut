organization in ThisBuild := "org.tpolecat"

version in ThisBuild := "0.4.6-SNAPSHOT"

publishArtifact := false

lazy val core = project.in(file("core"))

lazy val plugin = project.in(file("plugin"))

lazy val tests = project.in(file("tests"))

licenses in ThisBuild += ("MIT", url("http://opensource.org/licenses/MIT"))

lazy val tutPublishLocal = TaskKey[Unit]("tutPublishLocal", "publish core, plugin locally")

tutPublishLocal := {
  (publishLocal in core).value
  (publishLocal in plugin).value
}

resolvers in ThisBuild += Resolver.typesafeIvyRepo("releases")
