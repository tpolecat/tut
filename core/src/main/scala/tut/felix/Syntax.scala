package tut.felix

import java.io.OutputStream
import scala.annotation.tailrec

trait Syntax {

  implicit class MonadOps[M[_], A](ma: M[A])(implicit M: Monad[M]) {
    def map[B](f: A => B): M[B] = M.map(ma)(f)
    def flatMap[B](f: A => M[B]): M[B] = M.flatMap(ma)(f)
    def >>=[B](f: A => M[B]): M[B] = M.flatMap(ma)(f)
    def >>[B](mb: M[B]): M[B] = M.flatMap(ma)(_ => mb)
    def void: M[Unit] = ma.map(_ => ())
    def as[B](b: B): M[B] = ma.map(_ => b)
  }

  implicit class IdOps[A](a: A) {
    def point[M[_]](implicit M: Monad[M]) = M.point(a)
    def <|(f: A => Any): A = { f(a); a }
  }

  implicit class Tuple2Ops[A](a: (A, A)) {
    def umap[B](f: A => B): (B, B) = (f(a._1), f(a._2))
  }

  implicit class IOOps[A](ma: IO[A]) {
    def liftIO[M[_]](implicit M: LiftIO[M]): M[A] = M.liftIO(ma)
    def using[B](f: A => IO[B])(implicit A: Resource[A]): IO[B] =
      ma.flatMap(a => f(a).flatMap(b => A.close(a).map(_ => b)))
    def ensuring[B](mb: IO[B]): IO[A] =
      IO(try ma.unsafePerformIO finally void(mb.unsafePerformIO))
    def withOut(o: OutputStream): IO[A] =
      IO(Console.withOut(o)(ma.unsafePerformIO))
  }

  implicit class ListOps[A](as: List[A]) {
    def traverse[M[_]: Monad, B](f: A => M[B]): M[List[B]] =
      as.foldRight(List.empty[B].point[M])((a, mlb) => f(a).flatMap(b => mlb.map(b :: _)))

    def zipWithIndexAndNext[B](z: => B)(f: A => B): List[(A, B, Int)] = {
      @tailrec
      def fold(xs: List[(A, Int)], acc: List[(A, B, Int)]): List[(A, B, Int)] =
        xs match {
          case (x, i) :: (xs @ ((y, _) :: _)) => fold(xs, acc :+ ((x, f(y), i)))
          case (x, i) :: Nil => acc :+ ((x, z, i))
          case Nil => Nil
        }
      fold(as.zipWithIndex, Nil)
    }
  }

  implicit class BooleanOps(b: Boolean) {
    def whenM[M[_]: Monad, A](ma: M[A]): M[Unit] = if (b) ma.void else ().point[M]
    def unlessM[M[_]: Monad, A](ma: M[A]): M[Unit] = if (!b) ma.void else ().point[M]
    def fold[A](t: => A, f: => A): A = if (b) t else f
  }

  def void[A](a: A): Unit = ()

}
