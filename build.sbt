organization in ThisBuild := "org.tpolecat"

version in ThisBuild := "0.6.1-SNAPSHOT"

publishArtifact := false

lazy val core = project.in(file("core"))

lazy val plugin = project.in(file("plugin"))

licenses in ThisBuild += ("MIT", url("http://opensource.org/licenses/MIT"))

resolvers in ThisBuild += Resolver.typesafeIvyRepo("releases")
