package tut

import sbt._
import sbt.Keys._
import sbt.Defaults.runnerInit
import sbt.Attributed.data

object Plugin extends sbt.Plugin {
  
  lazy val tut = TaskKey[Unit]("tut", "create tut documentation")
  lazy val tutSourceDirectory = SettingKey[File]("tutSourceDirectory", "where to look for tut sources")

  override val settings =
    Seq(
      libraryDependencies += "org.tpolecat" %% "tut-core" % "0.1",
      tutSourceDirectory := sourceDirectory.value / "main" / "tut",
      tut := {
        val r   = (runner in (Compile, doc)).value
        val in  = tutSourceDirectory.value
        val out = crossTarget.value / "tut"
        val cp  = (fullClasspath in (Compile, doc)).value
        toError(r.run("tut.TutMain", 
                      data(cp), 
                      Seq(in.getAbsolutePath, out.getAbsolutePath), 
                      streams.value.log))
      }
    )

}

