resolvers += Resolver.url(
  "bintray-sbt-plugin-releases",
    url("http://dl.bintray.com/content/sbt/sbt-plugin-releases"))(
        Resolver.ivyStylePatterns)

addSbtPlugin("me.lessis" % "bintray-sbt" % "0.1.1")

resolvers ++= Seq(Resolver.typesafeRepo("releases"), Resolver.typesafeIvyRepo("releases"))

libraryDependencies += "org.scala-sbt" % "scripted-plugin" % sbtVersion.value
