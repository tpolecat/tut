package tut.felix

import scala.annotation.tailrec

/** A stacksafe IO monad derived from cats.data.Eval, originally written by Erik Osheim. */
sealed abstract class IO[+A] { self =>

  def unsafePerformIO(): A

  def map[B](f: A => B): IO[B] =
    flatMap(a => IO.point(f(a)))

  def flatMap[B](f: A => IO[B]): IO[B] =
    this match {
      case c: IO.Compute[A] =>
        new IO.Compute[B] {
          type Start = c.Start
          val start: () => IO[Start] = c.start
          val run: Start => IO[B] = (s: c.Start) =>
            new IO.Compute[B] {
              type Start = A
              val start = () => c.run(s)
              val run = f
            }
        }
      case c: IO.Call[A] =>
        new IO.Compute[B] {
          type Start = A
          val start = c.thunk
          val run = f
        }
      case _ =>
        new IO.Compute[B] {
          type Start = A
          val start = () => self
          val run = f
        }
    }

}

object IO {

  def apply[A](a: => A): IO[A] =
    new Primitive(a _)

  def point[A](a: A): IO[A] =
    Pure(a)

  val unit: IO[Unit] =
    Pure(())

  def putStrLn(s: String): IO[Unit] =
    IO(Console.println(s))

  def fail[A](t: Throwable): IO[A] =
    IO(throw t)

  implicit val MonadIO: Monad[IO] =
    new Monad[IO] {
      def point[A](a: A) = IO.point(a)
      def map[A, B](fa: IO[A])(f: A => B) = fa.map(f)
      def flatMap[A, B](fa: IO[A])(f: A => IO[B]) = fa.flatMap(f)
    }

  implicit val LiftIOIO: LiftIO[IO] =
    new LiftIO[IO] {
      def liftIO[A](ioa: IO[A]) = ioa
    }

  private final case class Pure[A](value: A) extends IO[A] {
    def unsafePerformIO() = value
  }

  private final class Primitive[A](f: () => A) extends IO[A] {
    def unsafePerformIO(): A = f()
  }

  private sealed abstract class Call[A](val thunk: () => IO[A]) extends IO[A] {
    def unsafePerformIO(): A = Call.loop(this).unsafePerformIO()
  }

  private object Call {

    @tailrec private def loop[A](fa: IO[A]): IO[A] = fa match {
      case call: IO.Call[A] =>
        loop(call.thunk())
      case compute: IO.Compute[A] =>
        new IO.Compute[A] {
          type Start = compute.Start
          val start: () => IO[Start] = () => compute.start()
          val run: Start => IO[A] = s => loop1(compute.run(s))
        }
      case other => other
    }

    private def loop1[A](fa: IO[A]): IO[A] = loop(fa)

  }

  private sealed abstract class Compute[A] extends IO[A] {
    type Start
    val start: () => IO[Start]
    val run: Start => IO[A]
    def unsafePerformIO(): A = {
      type L = IO[Any]
      type C = Any => IO[Any]
      @tailrec def loop(curr: L, fs: List[C]): Any =
        curr match {
          case c: Compute[_] =>
            c.start() match {
              case cc: Compute[_] =>
                loop(
                  cc.start().asInstanceOf[L],
                  cc.run.asInstanceOf[C] :: c.run.asInstanceOf[C] :: fs)
              case xx =>
                loop(c.run(xx.unsafePerformIO()), fs)
            }
          case x =>
            fs match {
              case f :: fs => loop(f(x.unsafePerformIO()), fs)
              case Nil => x.unsafePerformIO()
            }
        }
      loop(this.asInstanceOf[L], Nil).asInstanceOf[A]
    }
  }

}
