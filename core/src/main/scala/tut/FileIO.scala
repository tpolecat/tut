package tut

import java.io.{File, FileInputStream, FileOutputStream, OutputStreamWriter, PrintStream, PrintWriter}

import scala.tools.nsc.Settings
import scala.tools.nsc.interpreter.IMain
import scala.util.matching.Regex

import scala.io.Source

import tut.Zed._

object FileIO {
  val Encoding = "UTF-8"

  /**
   * Process a file or directory through tut if the filename matches the filter, otherwise
   * copy file as is.
   *
   * @param in File or directory to process
   * @param dir Target directory to output tut result in
   * @param filter Matcher for filenames to run tut on (e.g. *.md)
   * @param opts Tut options
   * @return Ending states of the tut processor after processing
   */
  def walk(in: File, dir: File, filter: Regex, opts: List[String]): IO[List[TutState]] =
    IO(dir.mkdirs) >> {
      if (in.isFile) {
        val out = new File(dir, in.getName)
        if (filter.pattern.matcher(in.getName).matches)
          (IO.putStrLn("[tut] compiling: " + in.getPath) >> FileIO.tut(in, out, opts)).map(List(_))
        else
          FileIO.cp(in, out).as(Nil)
      } else {
        FileIO.ls(in) >>= (_.traverse(f => walk(f, new File(dir, in.getName), filter, opts)).map(_.flatten))
      }
    }

  /**
   * Run the file through tut.
   *
   * @param in File to process
   * @param out File to write result to
   * @param opts Tut options
   * @return Ending state of tut after processing
   */
  def tut(in: File, out: File, opts: List[String]): IO[TutState] =
    IO(new FileOutputStream(out)).using                     { outputStream =>
    IO(new AnsiFilterStream(outputStream)).using            { filterStream =>
    IO(new Spigot(filterStream)).using                      { filterSpigot =>
    IO(new PrintStream(filterSpigot, true, Encoding)).using { printStream =>
    IO(new OutputStreamWriter(printStream, Encoding)).using { streamWriter =>
    IO(new PrintWriter(streamWriter)).using                 { printWriter =>
      (for {
        interp <- newInterpreter(printWriter, opts)
        state  =  TutState(false, Set(), false, interp, printWriter, filterSpigot, "", false, in, opts)
        endSt  <- Tut.file(in).exec(state)
      } yield endSt).withOut(printStream)
    }}}}}}

  private def newInterpreter(pw: PrintWriter, opts: List[String]): IO[IMain] =
    IO(new IMain(new Settings <| (_.embeddedDefaults[TutMain.type]) <| (_.processArguments(opts, true)), pw))

  private[tut] def ls(dir: File): IO[List[File]] =
    IO(Option(dir.listFiles).fold(List.empty[File])(_.toList))

  private[tut] def cp(src: File, dst: File): IO[Unit] =
    IO(new FileInputStream(src)).using  { in =>
    IO(new FileOutputStream(dst)).using { out =>
      IO {
        val buf = new Array[Byte](1024 * 16)
        var count = 0
        while ({ count = in.read(buf); count >= 0 })
          out.write(buf, 0, count)
      }
    }}

  private[tut] def lines(f: File): IO[List[String]] =
    IO(Source.fromFile(f, Encoding).getLines.toList)
}
