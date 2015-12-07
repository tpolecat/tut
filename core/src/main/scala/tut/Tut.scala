package tut

import scala.io.Source
import scala.tools.nsc.Settings
import scala.tools.nsc.interpreter.IMain
import scala.tools.nsc.interpreter.Results
import scala.util.matching.Regex

import java.io.ByteArrayOutputStream
import java.io.Closeable
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FilterOutputStream
import java.io.OutputStreamWriter
import java.io.OutputStream
import java.io.PrintStream
import java.io.PrintWriter
import java.io.Writer

object TutMain extends Zed {

  ////// TYPES FOR OUR WEE INTERPRETER

  sealed trait Modifier
  case object NoFail    extends Modifier
  case object Fail      extends Modifier
  case object Silent    extends Modifier
  case object Book      extends Modifier
  case object Plain     extends Modifier
  case object Invisible extends Modifier
  case object Reset     extends Modifier

  object Modifier {
    def fromString(s: String): Option[Modifier] =
      Some(s) collect {
        case "nofail"    => NoFail
        case "fail"      => Fail
        case "silent"    => Silent
        case "book"      => Book
        case "plain"     => Plain
        case "invisible" => Invisible
        case "reset"     => Reset
      }

    def unsafeFromString(s: String): Modifier =
      fromString(s).getOrElse(throw new RuntimeException("No such modifier: " + s))
  }

  case class TState(
    isCode: Boolean,
    mods: Set[Modifier],
    needsNL: Boolean,
    imain: IMain,
    pw: PrintWriter,
    spigot: Spigot,
    partial: String,
    err: Boolean,
    in: File,
    opts: List[String])

  type Tut[A] = StateT[IO, TState, A]
  def state: Tut[TState] = get.lift[IO]
  def mod(f: TState => TState): Tut[Unit] = modify(f).lift[IO]

  ////// YIKES

  class Spigot(os: OutputStream) extends FilterOutputStream(os) {
    private var baos = new ByteArrayOutputStream()
    def bytes = baos.toByteArray

    private[this] var active = true
    private[this] def ifActive(f: => Unit): Unit = if (active) f
    def setActive(b: Boolean): IO[Unit] = IO { baos.reset(); active = b }

    private[this] var replInput: Option[String] = None
    def commentAfter(text: String): IO[Unit] = IO { replInput = Some(text) }
    def stopCommenting(): IO[Unit] = IO { replInput = None }
    private[this] var output = new StringBuilder()
    private[this] def comment(): Unit = "// ".map(_.toInt).foreach(write)
    private[this] var wasNL: Boolean = false

    override def write(n: Int): Unit = {
      val commenting: Boolean = replInput.exists(output.indexOf(_) != 1)
      if (wasNL && commenting) { wasNL = false; comment() }
      output.append(n.toChar)
      baos.write(n); ifActive(super.write(n))
      wasNL = (n == '\n'.toInt)
     }
  }

  ////// ENTRY POINT

  def main(args: Array[String]): Unit =
    runl(args.toList).unsafePerformIO

  def runl(args: List[String]): IO[Unit] = {
    val (in, out) = (args(0), args(1)).umap(new File(_))
    val filter = args(2).r
    val opts   = args.drop(3)
    for {
      fa <- if (in.isFile) IO(List(in)) else ls(in)
      ss <- fa.traverse(f => walk(f, out, filter, opts)).map(_.flatten)
    } yield {
      if (ss.exists(_.err)) throw new Exception("Tut execution failed.")
      else ()
    }
  }

  ////// IO ACTIONS

  val Encoding = "UTF-8"

  def ls(dir: File): IO[List[File]] =
    IO(Option(dir.listFiles).fold(List.empty[File])(_.toList))

  def walk(in: File, dir: File, filter: Regex, opts: List[String]): IO[List[TState]] =
    IO(dir.mkdirs) >> {
      if (in.isFile) {
        val out = new File(dir, in.getName)
        if (filter.pattern.matcher(in.getName).matches)
          go(in, out, opts).map(List(_))
        else
          copyFile(in, out).as(Nil)
      } else {
        ls(in) >>= (_.traverse(f => walk(f, new File(dir, in.getName), filter, opts)).map(_.flatten))
      }
    }

  def copyFile(src: File, dst: File): IO[Unit] =
    IO(new FileInputStream(src)).using  { in =>
    IO(new FileOutputStream(dst)).using { out =>
      IO {
        val buf = new Array[Byte](1024 * 16)
        var count = 0
        while ({ count = in.read(buf); count >= 0 })
          out.write(buf, 0, count)
      }
    }}

  def go(in: File, out: File, opts: List[String]): IO[TState] =
    putStrLn("[tut] compiling: " + in.getPath) >> file(in, out, opts)

  def file(in: File, out: File, opts: List[String]): IO[TState] =
    IO(new FileOutputStream(out)).using           { f =>
    IO(new AnsiFilterStream(f)).using             { a =>
    IO(new Spigot(a)).using                       { o =>
    IO(new PrintStream(o, true, Encoding)).using  { s =>
    IO(new OutputStreamWriter(s, Encoding)).using { w =>
    IO(new PrintWriter(w)).using                  { p =>
      for {
        oo <- IO(Console.out)
        _  <- IO(Console.setOut(s))
        i  <- newInterpreter(p, opts)
        ts <- tut(in).exec(TState(false, Set(), false, i, p, o, "", false, in, opts)).ensuring(IO(Console.setOut(oo)))
      } yield ts
    }}}}}}

  def newInterpreter(pw: PrintWriter, opts: List[String]): IO[IMain] =
    IO(new IMain(new Settings <| (_.embeddedDefaults[TutMain.type]) <| (_.processArguments(opts, true)), pw))

  def lines(f: File): IO[List[String]] =
    IO(Source.fromFile(f, Encoding).getLines.toList)

  ////// TUT ACTIONS

  def tut(in: File): Tut[Unit] =
    for {
      t <- lines(in).liftIO[Tut]
      _ <- t.zipWithIndex.traverse { case (t, n) => line(t, n + 1) }
    } yield ()

  def checkBoundary(text: String, find: String, code: Boolean, mods: Set[Modifier]): Tut[Unit] =
    (text.trim.startsWith(find)).whenM(mod(s => s.copy(isCode = code, needsNL = false, mods = mods)))

  def fixShed(text: String, mods: Set[Modifier]): String =
    if (mods(Invisible)) {
      ""
    } else if (text.startsWith("```tut")) {
      if (mods(Plain)) "```" else "```scala"
    } else {
      text
    }

  def modifiers(text: String): Set[Modifier] =
    if (text.startsWith("```tut:"))
      text.split(":").toList.tail.map(Modifier.unsafeFromString).toSet
    else
      Set.empty

  def line(text: String, n: Int): Tut[Unit] =
    for {
      s <- state
      inv = s.mods.filter(_ == Invisible)
      _ <- checkBoundary(text, "```", false, Set())
      s <- state
      mods = modifiers(text)
      _ <- IO {
        if (mods(Reset)) {
          s.imain.reset()
          s.imain.settings.processArguments(s.opts, true)
          s
        } else {
          s
        }
      }.liftIO[Tut]
      _ <- s.isCode.fold(interp(text, n), out(fixShed(text, mods ++ inv)))
      _ <- checkBoundary(text, "```tut", true, mods)
    } yield ()

  def out(text: String): Tut[Unit] =
    for {
      s <- state
      _ <- s.mods(Invisible).unlessM(IO { s.pw.println(text); s.pw.flush() }.liftIO[Tut])
    } yield ()

  def success: Tut[Unit] =
    for {
      s <- state
      _ <- new String(s.spigot.bytes, Encoding).endsWith("...").whenM(out("")) // #29
      _ <- mod(s => s.copy(needsNL = !s.mods(Silent), partial = ""))
    } yield ()

  def incomplete(s: String): Tut[Unit] =
    mod(a => a.copy(partial = a.partial + "\n" + s, needsNL = false))

  def error(n: Int, msg: Option[String] = None): Tut[Unit] =
    for {
      s <- state
      _ <- mod(_.copy(err = true))
      _ <- IO(Console.err.println(f"[tut] *** Error reported at ${s.in.getName}%s:$n%d${msg.fold("")(": " + _)}%s")).liftIO[Tut]
      _ <- IO(Console.err.write(s.spigot.bytes)).liftIO[Tut]
    } yield ()

  def prompt(s: TState): String =
         if (s.mods(Silent) || s.mods(Book)) ""
    else if (s.partial.isEmpty) "scala> "
    else                        "     | "

  def interp(text: String, lineNum: Int): Tut[Unit] =
    state >>= { s =>
      (text.trim.nonEmpty || s.partial.nonEmpty || s.mods(Silent)).whenM[Tut,Unit] {
        for {
          _ <- s.needsNL.whenM(out(""))
          _ <- (s.mods(Invisible)).unlessM(out(prompt(s) + text))
          _ <- s.spigot.setActive(!(s.mods(Silent) || (s.mods(Invisible)))).liftIO[Tut]
          _ <- s.mods(Book).whenM(s.spigot.commentAfter(s.partial + "\n" + text).liftIO[Tut])
          r <- IO(s.imain.interpret(s.partial + "\n" + text)).liftIO[Tut] >>= {
            case Results.Incomplete => incomplete(text)
            case Results.Success    => if (s.mods(Fail)) error(lineNum, Some("failure was asserted but no failure occurred")) else success
            case Results.Error      => if (s.mods(NoFail) || s.mods(Fail)) success else error(lineNum)
          }
          _ <- s.mods(Book).whenM(s.spigot.stopCommenting().liftIO[Tut])
          _ <- s.spigot.setActive(true).liftIO[Tut]
          _ <- IO(s.pw.flush).liftIO[Tut]
        } yield ()
      }
    }
}


