name := "tut-core"

libraryDependencies ++= Seq(
  "org.scalaz"     %% "scalaz-core"    % "7.0.6",
  "org.scalaz"     %% "scalaz-effect"  % "7.0.6",
  "org.scala-lang" %  "scala-compiler" % scalaVersion.value
)

seq(bintrayPublishSettings:_*)
