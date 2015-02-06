package tut

import sbt._
import sbt.Keys._
import sbt.Defaults.runnerInit
import sbt.Attributed.data

object Plugin extends sbt.Plugin {
  
  lazy val tut = TaskKey[Seq[File]]("tut", "create tut documentation")
  lazy val tutSourceDirectory = SettingKey[File]("tutSourceDirectory", "where to look for tut sources")

  def safeListFiles(dir: File): List[File] =
    Option(dir.listFiles).fold(List.empty[File])(_.toList)

  lazy val tutSettings =
    Seq(
      resolvers += "tpolecat" at "http://dl.bintray.com/tpolecat/maven",
      libraryDependencies += "org.tpolecat" %% "tut-core" % "0.4.0-SNAPSHOT",
      tutSourceDirectory := sourceDirectory.value / "main" / "tut",
      watchSources <++= tutSourceDirectory map { path => (path ** "*.md").get },
      tut := {
        val r   = (runner in (Compile, doc)).value
        val in  = tutSourceDirectory.value
        val out = crossTarget.value / "tut"
        val cp  = (fullClasspath in (Compile, doc)).value
        toError(r.run("tut.TutMain", 
                      data(cp), 
                      Seq(in.getAbsolutePath, out.getAbsolutePath), 
                      streams.value.log))
        // We can't return a value from the runner, but we know what TutMain is looking at so we'll
        // fake it here. Returning all files potentially touched.
        val read = safeListFiles(in).map(_.getName).toSet
        safeListFiles(out).filter(f => read(f.getName))
      }
    )

}

