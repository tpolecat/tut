package tut

import scalaz._
import Scalaz._
import scalaz.effect._
import scalaz.effect.IO._

import scala.io.Source
import scala.tools.nsc.Settings
import scala.tools.nsc.interpreter.IMain
import scala.tools.nsc.interpreter.Results

import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.io.PrintWriter

object TutMain extends SafeApp {

  ////// TYPES FOR OUR WEE INTERPRETER

  case class TState(isCode: Boolean, imain: IMain, pw: PrintWriter)

  type Tut[+A] = StateT[IO, TState, A]

  object Tut {
    def apply[A](a: => A): Tut[A] = StateT(s => IO((s, a)))
  }

  implicit val TutMonadIO = new MonadIO[Tut] {
    def point[A](a: => A): Tut[A] = Tut(a)
    def bind[A, B](fa: Tut[A])(f: A => Tut[B]): Tut[B] = fa.flatMap(f)
    def liftIO[A](ioa: IO[A]): Tut[A] = StateT(ioa.strengthL)
  }

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
      _ <- tut(f).eval(TState(false, i, p))
      _ <- IO(p.close)
      _ <- IO(w.close)
      _ <- IO(o.close)
    } yield ()  

  def newInterpreter(pw: PrintWriter): IO[IMain] =
    IO(new IMain(new Settings <| (_.embeddedDefaults[Tut.type]), pw))

  def lines(f: File): IO[List[String]] =
    IO(Source.fromFile(f, Encoding).getLines.toList)

  ////// TUT ACTIONS

  def tut(f: File): Tut[Unit] =
    for {
      t <- lines(f).liftIO[Tut]
      _ <- t.zipWithIndex.traverseU { case (t, n) => line(t, n + 1) }
    } yield ()

  def checkBoundary(text: String, s: String, b: Boolean): Tut[Unit] =
    (text.trim === s).whenM(mod(s => s.copy(isCode = b)))

  def line(text: String, n: Int): Tut[Unit] =
    for {
      _ <- checkBoundary(text, "```", false)
      s <- state
      _ <- s.isCode.fold(interp(text, n), out(text))
      _ <- checkBoundary(text, "```scala", true)
    } yield () 

  def out(s: String): Tut[Unit] =
    state.map(_.pw.println(s))

  def interp(s: String, n: Int): Tut[Unit] =
    (!s.trim.isEmpty).whenM[Tut,Unit] {
      for {
        _ <- out("scala> " + s)
        i <- state.map(_.imain)
        r <- IO(i.interpret(s)).liftIO[Tut] >>= {
          case Results.Success => Tut(())
          case _ => Tut(Console.err.println(f"\terror reported at source line $n%d"))
        }
        _ <- out("")
      } yield ()
    }

}