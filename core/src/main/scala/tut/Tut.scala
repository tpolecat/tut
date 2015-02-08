package tut

import scalaz._
import Scalaz._
import scalaz.effect._
import scalaz.effect.stateTEffect._
import scalaz.effect.IO._
import scalaz.std.effect.closeable._

import scala.io.Source
import scala.tools.nsc.Settings
import scala.tools.nsc.interpreter.IMain
import scala.tools.nsc.interpreter.Results

import java.io.ByteArrayOutputStream
import java.io.Closeable
import java.io.File
import java.io.FileOutputStream
import java.io.FilterOutputStream
import java.io.OutputStreamWriter
import java.io.OutputStream
import java.io.PrintStream
import java.io.PrintWriter
import java.io.Writer

object TutMain extends SafeApp {

  ////// TYPES FOR OUR WEE INTERPRETER

  sealed trait Modifier
  case object NoFail extends Modifier
  case object Silent extends Modifier
  case object Plain  extends Modifier

  object Modifier {
    def fromString(s: String): Option[Modifier] =
      Some(s) collect {
        case "nofail" => NoFail
        case "silent" => Silent
        case "plain"  => Plain
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
    in: File)

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
    override def write(n: Int): Unit = { baos.write(n); ifActive(super.write(n)) }
    override def write(bs: Array[Byte]): Unit = { ifActive(super.write(bs)) }
    override def write(bs: Array[Byte], off: Int, len: Int): Unit = { ifActive(super.write(bs, off, len)) }
  }

  ////// ENTRY POINT

  override def runl(args: List[String]): IO[Unit] = {
    val (in, out) = (args(0), args(1)).umap(new File(_))
    for {
      _  <- IO(out.mkdirs)
      fa <- IO(Option(in.listFiles).map(_.toList).orZero)
      fb <- stale(fa, out)
      ss <- fb.traverseU(in => go(in, new File(out, in.getName)))
    } yield {
      if (ss.exists(_.err)) throw new Exception("Tut execution failed.")
      else ()
    }
  }

  def stale(fs: List[File], outDir: File): IO[List[File]] =
    IO(fs)
    // IO(fs.filter(f => (new File(outDir, f.getName)).lastModified < f.lastModified))

  ////// IO ACTIONS

  val Encoding = "UTF-8"

  def go(in: File, out: File): IO[TState] =
    putStrLn("[tut] compiling: " + in.getPath) >> file(in, out)

  def file(in: File, out: File): IO[TState] =
    IO(new FileOutputStream(out)).using           { f =>
    IO(new AnsiFilterStream(f)).using             { a =>
    IO(new Spigot(a)).using                       { o =>
    IO(new PrintStream(o, true, Encoding)).using  { s =>
    IO(new OutputStreamWriter(s, Encoding)).using { w =>
    IO(new PrintWriter(w)).using                  { p =>
      for {
        oo <- IO(Console.out)
        _  <- IO(Console.setOut(s))
        i  <- newInterpreter(p)
        ts <- tut(in).exec(TState(false, Set(), false, i, p, o, "", false, in)).ensuring(IO(Console.setOut(oo)))
      } yield ts
    }}}}}}

  def newInterpreter(pw: PrintWriter): IO[IMain] =
    IO(new IMain(new Settings <| (_.embeddedDefaults[TutMain.type]), pw))

  def lines(f: File): IO[List[String]] =
    IO(Source.fromFile(f, Encoding).getLines.toList)

  ////// TUT ACTIONS

  def tut(in: File): Tut[Unit] =
    for {
      t <- lines(in).liftIO[Tut]
      _ <- t.zipWithIndex.traverseU { case (t, n) => line(t, n + 1) }
    } yield ()

  def checkBoundary(text: String, find: String, code: Boolean, mods: Set[Modifier]): Tut[Unit] =
    (text.trim.startsWith(find)).whenM(mod(s => s.copy(isCode = code, needsNL = false, mods = mods)))

  def fixShed(text: String, mods: Set[Modifier]): String = 
    if (text.startsWith("```tut")) {
      if (mods(Plain)) "```" else "```scala" 
    } else text

  def modifiers(text: String): Set[Modifier] =
    if (text.startsWith("```tut:")) 
      text.split(":").toList.tail.map(Modifier.unsafeFromString).toSet
    else
      Set.empty

  def line(text: String, n: Int): Tut[Unit] =
    for {
      _ <- checkBoundary(text, "```", false, Set())
      s <- state
      mods = modifiers(text)
      _ <- s.isCode.fold(interp(text, n), out(fixShed(text, mods)))
      _ <- checkBoundary(text, "```tut", true, mods)
    } yield ()

  def out(text: String): Tut[Unit] =
    state >>= (s => IO { s.pw.println(text); s.pw.flush() }.liftIO[Tut])

  def success: Tut[Unit] =
    mod(s => s.copy(needsNL = true, partial = ""))

  def incomplete(s: String): Tut[Unit] =
    mod(a => a.copy(partial = a.partial + "\n" + s, needsNL = false))

  def error(n: Int): Tut[Unit] =
    for {
      s <- state
      _ <- mod(_.copy(err = true))
      _ <- IO(Console.err.println(f"[tut] *** Error reported at ${s.in.getName}%s:$n%d")).liftIO[Tut]
      _ <- IO(Console.err.write(s.spigot.bytes)).liftIO[Tut]
    } yield ()

  def prompt(s: TState): String =
         if (s.mods(Silent))  ""
    else if (s.partial.isEmpty) "scala> "
    else                        "     | "

  def interp(text: String, lineNum: Int): Tut[Unit] =
    text.trim.isEmpty.unlessM[Tut,Unit] {
      for {
        s <- state
        _ <- s.needsNL.whenM(out(""))
        _ <- out(prompt(s) + text)
        _ <- s.spigot.setActive(!s.mods(Silent)).liftIO[Tut]
        r <- IO(s.imain.interpret(s.partial + "\n" + text)).liftIO[Tut] >>= {
          case Results.Success    => success
          case Results.Incomplete => incomplete(text)
          case Results.Error      => if (s.mods(NoFail)) success else error(lineNum)
        }
        _ <- s.spigot.setActive(true).liftIO[Tut]
        _ <- IO(s.pw.flush).liftIO[Tut]
      } yield ()
    }

}


