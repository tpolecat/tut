package tut

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

object TutMain extends Zed {

  ////// TYPES FOR OUR WEE INTERPRETER

  sealed trait Modifier
  case object NoFail    extends Modifier
  case object Fail      extends Modifier
  case object Silent    extends Modifier
  case object Plain     extends Modifier
  case object Invisible extends Modifier

  object Modifier {
    def fromString(s: String): Option[Modifier] =
      Some(s) collect {
        case "nofail"    => NoFail
        case "fail"      => Fail
        case "silent"    => Silent
        case "plain"     => Plain
        case "invisible" => Invisible
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

  def main(args: Array[String]): Unit =
    runl(args.toList).unsafePerformIO

  def runl(args: List[String]): IO[Unit] = {
    val (in, out) = (args(0), args(1)).umap(new File(_))
    val opts = args.drop(2)
    for {
      _  <- IO(out.mkdirs)
      fa <- IO { 
        if (in.isFile) List(in)
        else Option(in.listFiles).fold(List.empty[File])(_.toList) 
      }
      ss <- fa.traverse(f => go(f, new File(out, f.getName), opts))
    } yield {
      if (ss.exists(_.err)) throw new Exception("Tut execution failed.")
      else ()
    }
  }

  ////// IO ACTIONS

  val Encoding = "UTF-8"

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
        ts <- tut(in).exec(TState(false, Set(), false, i, p, o, "", false, in)).ensuring(IO(Console.setOut(oo)))
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
      _ <- s.isCode.fold(interp(text, n), out(fixShed(text, mods ++ inv)))
      _ <- checkBoundary(text, "```tut", true, mods)
    } yield ()

  def out(text: String): Tut[Unit] =
    for {
      s <- state
      _ <- s.mods(Invisible).unlessM(IO { s.pw.println(text); s.pw.flush() }.liftIO[Tut])
    } yield ()

  def success: Tut[Unit] =
    mod(s => s.copy(needsNL = true, partial = ""))

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
         if (s.mods(Silent))  ""
    else if (s.partial.isEmpty) "scala> "
    else                        "     | "

  def interp(text: String, lineNum: Int): Tut[Unit] =
    text.trim.isEmpty.unlessM[Tut,Unit] {
      for {
        s <- state
        _ <- s.needsNL.whenM(out(""))
        _ <- (s.mods(Invisible)).unlessM(out(prompt(s) + text))
        _ <- s.spigot.setActive(!(s.mods(Silent) || (s.mods(Invisible)))).liftIO[Tut]
        r <- IO(s.imain.interpret(s.partial + "\n" + text)).liftIO[Tut] >>= {
          case Results.Incomplete => incomplete(text)
          case Results.Success    => if (s.mods(Fail)) error(lineNum, Some("failure was asserted but no failure occurred")) else success
          case Results.Error      => if (s.mods(NoFail) || s.mods(Fail)) success else error(lineNum)
        }
        _ <- s.spigot.setActive(true).liftIO[Tut]
        _ <- IO(s.pw.flush).liftIO[Tut]
      } yield ()
    }

}


