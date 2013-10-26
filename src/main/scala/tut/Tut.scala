package tut

import scalaz._
import Scalaz._
import scalaz.effect._
import scalaz.effect.stateTEffect._
import scalaz.effect.IO._

import scala.io.Source
import scala.tools.nsc.Settings
import scala.tools.nsc.interpreter.IMain
import scala.tools.nsc.interpreter.Results

import java.io.Closeable
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.io.PrintWriter

object TutMain extends SafeApp {

  ////// TYPES FOR OUR WEE INTERPRETER

  case class TState(isCode: Boolean, needsNL: Boolean, imain: IMain, pw: PrintWriter, partial: String = "") {
    def +(s: String) = copy(partial = partial + "\n" + s, needsNL = false)
  }

  type Tut[+A] = StateT[IO, TState, A]
  def state: Tut[TState] = get.lift[IO]
  def mod(f: TState => TState): Tut[Unit] = modify(f).lift[IO]

  ////// ENTRY POINT

  override def runc: IO[Unit] = 
    for {
      d <- IO(new File("src/main/tut").listFiles.toList)
      _ <- d.traverseU(file)
    } yield ()

  ////// IO ACTIONS

  val Encoding = "UTF-8"

  def file(f: File): IO[Unit] = 
    for {
      _ <- putStrLn(f.getPath)
      o <- IO(new FileOutputStream(new File("out", f.getName)))
      w <- IO(new OutputStreamWriter(o, Encoding))
      p <- IO(new PrintWriter(w))
      i <- newInterpreter(p)
      _ <- tut(f).eval(TState(false, false, i, p))
      _ <- IO(p.close)
      _ <- IO(w.close)
      _ <- IO(o.close)
    } yield ()  

  def closing[A <: Closeable, B](a: => A)(f: A => IO[B]): IO[B] =
    IO(a) >>= (a => f(a).ensuring(IO(a.close)))

  def newInterpreter(pw: PrintWriter): IO[IMain] =
    IO(new IMain(new Settings <| (_.embeddedDefaults[TutMain.type]), pw))

  def lines(f: File): IO[List[String]] =
    IO(Source.fromFile(f, Encoding).getLines.toList)

  ////// TUT ACTIONS

  def tut(f: File): Tut[Unit] =
    for {
      t <- lines(f).liftIO[Tut]
      _ <- t.zipWithIndex.traverseU { case (t, n) => line(t, n + 1) }
    } yield ()

  def checkBoundary(text: String, s: String, b: Boolean): Tut[Unit] =
    (text.trim === s).whenM(mod(s => s.copy(isCode = b, needsNL = false)))

  def line(text: String, n: Int): Tut[Unit] =
    for {
      _ <- checkBoundary(text, "```", false)
      s <- state
      _ <- s.isCode.fold(interp(text, n), out(text))
      _ <- checkBoundary(text, "```scala", true)
    } yield () 

  def out(s: String): Tut[Unit] =
    state.map(_.pw.println(s))

  def success: Tut[Unit] =
    mod(s => s.copy(needsNL = true, partial = ""))

  def incomplete(s: String): Tut[Unit] =
    mod(_ + s)

  def error(n: Int): Tut[Unit] =
    (Console.err.println(f"\terror reported at source line $n%d")).point[Tut]

  def interp(text: String, lineNum: Int): Tut[Unit] =
    (!text.trim.isEmpty).whenM[Tut,Unit] {
      for {
        s <- state
        _ <- s.needsNL.whenM(out(""))
        _ <- out(s.partial.isEmpty.fold("scala> ", "     | ") + text)
        r <- IO(s.imain.interpret(s.partial + "\n" + text)).liftIO[Tut] >>= {
          case Results.Success    => success
          case Results.Incomplete => incomplete(text)
          case Results.Error      => error(lineNum)
        }
      } yield ()
    }

}