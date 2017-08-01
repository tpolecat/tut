package tut

import scala.util.matching.Regex

import sbt._
import sbt.Keys._
import sbt.Attributed.data
import sbt.complete.Parser
import sbt.complete.DefaultParsers._
import sbt.librarymanagement.ConfigRef
import sbt.internal.io.Source
import sbt.util.CacheStoreFactory

object TutPlugin extends AutoPlugin {

  override def trigger = noTrigger

  override def requires = sbt.plugins.JvmPlugin

  type Dir = File

  object autoImport {
    lazy val Tut                = (config("tut") extend Compile).hide
    lazy val tut                = taskKey[Seq[(File,String)]]("create tut documentation")
    lazy val tutSourceDirectory = settingKey[File]("where to look for tut sources")
    lazy val tutPluginJars      = taskKey[Seq[File]]("Plugin jars to be used by tut REPL.")
    lazy val tutOnly            = inputKey[Unit]("Run tut on a single file.")
    lazy val tutTargetDirectory = settingKey[File]("Where tut output goes")
    lazy val tutNameFilter      = settingKey[Regex]("tut skips files whose names don't match")
    lazy val tutQuick           = taskKey[Set[File]]("Run tut incrementally on recently changed files")
  }

  import autoImport._

  override lazy val projectSettings =
    inConfig(Tut)(Defaults.configSettings) ++
    Seq(
      resolvers += "tpolecat" at "http://dl.bintray.com/tpolecat/maven",
      libraryDependencies += "org.tpolecat" %% "tut-core" % BuildInfo.version % Tut,
      ivyConfigurations += Tut,
      tutSourceDirectory := (sourceDirectory in Compile).value / "tut",
      tutTargetDirectory := crossTarget.value / "tut",
      tutNameFilter := """.*\.(md|markdown|txt|htm|html)""".r,
      watchSources in Defaults.ConfigGlobal +=
        new Source(
          tutSourceDirectory.value,
          new NameFilter {
            override def accept(name: String): Boolean = tutNameFilter.value.pattern.matcher(name).matches()
          },
          NothingFilter
        ),
      scalacOptions in Tut := (scalacOptions in (Compile, console)).value,
      tutPluginJars := {
        // no idea if this is the right way to do this
        val deps = (libraryDependencies in Tut).value.filter(_.configurations.fold(false)(_.startsWith("plugin->")))
        update.value.configuration(ConfigRef("plugin")).map(_.modules).getOrElse(Nil).filter { m =>
          deps.exists { d =>
            d.organization == m.module.organization &&
            d.name         == m.module.name &&
            d.revision     == m.module.revision
          }
        }.flatMap(_.artifacts.map(_._2))
      },
      tut := {
        val r     = (runner in Tut).value
        val in    = tutSourceDirectory.value
        val out   = tutTargetDirectory.value
        val cp    = (fullClasspath in Tut).value
        val opts  = (scalacOptions in Tut).value
        val pOpts = tutPluginJars.value.map(f => "–Xplugin:" + f.getAbsolutePath)
        val re    = tutNameFilter.value.pattern.toString
        tutOne(streams.value, r, in, out, cp, opts, pOpts, re)
      },
      tutOnly := {
        val in = tutFilesParser.parsed
        val r     = (runner in Tut).value
        val inR   = tutSourceDirectory.value // input root
        val inDir = if (in.isDirectory) in
        else in.getParentFile    // input dir
        val outR  = tutTargetDirectory.value // output root
        val out   = new File(outR, inR.toURI.relativize(inDir.toURI).getPath) // output dir
        val cp    = (fullClasspath in Tut).value
        val opts  = (scalacOptions in Tut).value
        val pOpts = tutPluginJars.value.map(f => "–Xplugin:" + f.getAbsolutePath)
        val re    = tutNameFilter.value.pattern.toString
        tutOne(streams.value, r, in, out, cp, opts, pOpts, re)
      },
      tutQuick := {
        val r     = (runner in Tut).value
        val inR   = tutSourceDirectory.value
        val outR  = tutTargetDirectory.value
        val cp    = (fullClasspath in Tut).value
        val opts  = (scalacOptions in Tut).value
        val pOpts = tutPluginJars.value.map(f => "–Xplugin:" + f.getAbsolutePath)
        val re    = tutNameFilter.value.pattern.toString
        val cache = streams.value.cacheDirectory / "tut"

        def handleUpdate(inReport: ChangeReport[File], outReport: ChangeReport[File]) = {
          val in    = (inReport.modified -- inReport.removed).toList
          val out   = in.map { in =>
            val inDir = if (in.isDirectory) in else in.getParentFile    // input dir
            new File(outR, inR.toURI.relativize(inDir.toURI).getPath) // output dir
          }

          tutAll(streams.value, r, in.toList, out, cp, opts, pOpts, re).map(_._1).toSet
        }

        val files = safeListFiles(inR, recurse = true).toSet

        FileFunction.cached(CacheStoreFactory(cache), FilesInfo.hash, FilesInfo.exists)(handleUpdate)(files)
      }
    )


  def safeListFiles(dir: File, recurse: Boolean): List[File] =
    Option(dir.listFiles).fold(List.empty[File]){ files =>
      val l = files.toList
      if (recurse) l.flatMap(flatten) else l
    }

  def safeRelativize(in: File, out: File, recurse: Boolean): List[(File, String)] = {
    val inPath = in.toPath
    val outPath = out.toPath
    val read = safeListFiles(in, recurse = true).map(f => inPath.relativize(f.toPath)).toSet
    safeListFiles(out, recurse = true).flatMap { f =>
      val rel = outPath.relativize(f.toPath)
      if (read(rel)) (f -> rel.toString) :: Nil else Nil
    }
  }

  def flatten(f: File): List[File] =
    f :: (if (f.isDirectory) f.listFiles.toList.flatMap(flatten) else Nil)

  val tutFilesParser: Def.Initialize[State => Parser[File]] = Def.setting { (state: State) =>
    val extracted = Project.extract(state)
    val dir     = extracted.getOpt(tutSourceDirectory)
    val files   = dir.fold(List.empty[File])(d => safeListFiles(d, recurse = true))
    val parsers = dir.fold(List.empty[Parser[File]])(dir => files.map(f => literal(dir.toURI.relativize(f.toURI).getPath).map(_ => f)))
    val folded  = parsers.foldRight[Parser[File]](failure("<no input files>"))(_ | _)
    token(Space ~> folded)
  }

  /** Run the Tut CLI for a single input file or directory */
  def tutOne(streams: TaskStreams, r: ScalaRun, in: File, out: File, cp: Classpath, opts: Seq[String], pOpts: Seq[String], re: String): List[(File, String)] = {
    r.run("tut.TutMain",
      data(cp),
      Seq(in.getAbsolutePath, out.getAbsolutePath, re) ++ opts ++ pOpts,
      streams.log
    ).failed foreach (sys error _.getMessage)
    // We can't return a value from the runner, but we know what TutMain is looking at so we'll
    // fake it here. Returning all files potentially touched.
    safeRelativize(in, out, recurse = true)
  }

  /** Run the Tut CLI repeatedly for a list of input files or directories */
  def tutAll(streams: TaskStreams, r: ScalaRun, in: List[File], out: List[File], cp: Classpath, opts: Seq[String], pOpts: Seq[String], re: String): List[(File, String)] =
    (in zip out).flatMap { case (in, out) => tutOne(streams, r, in, out, cp, opts, pOpts, re ) }

}
