package tut

import java.io.OutputStream

object Zed {
  trait Monad[M[_]] {
    def point[A](a: A): M[A]
    def map[A, B](ma: M[A])(f: A => B): M[B]
    def flatMap[A, B](ma: M[A])(f: A => M[B]): M[B]
  }

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

  final case class State[S, A](run: S => (A, S)) {
    def map[B](f: A => B): State[S, B] = State { s => val (a, s0) = run(s); (f(a), s0) }
    def flatMap[B](f: A => State[S, B]): State[S, B] = State { s => val (a, s0) = run(s); f(a).run(s0) }
    def lift[M[_]: Monad]: StateT[M, S, A] = StateT { s => run(s).point[M] }
    def exec(s: S): S = run(s)._2
  }

  object State {
    implicit def StateInstances[S] = new Monad[({ type l[a] = State[S, a] })#l] {
      def point[A](a: A) = State(s => (a, s))
      def map[A,B](ma: State[S, A])(f: A => B) = ma.map(f)
      def flatMap[A,B](ma: State[S, A])(f: A => State[S, B]) = ma.flatMap(f)
    }

    def get[S]: State[S, S] = State(s => (s, s))

    def modify[S](f: S => S): State[S, Unit] = State(s => ((), f(s)))
  }

  trait LiftIO[M[_]] {
    def liftIO[A](ma: IO[A]): M[A]
  }

  final case class StateT[M[_]: Monad, S, A](run: S => M[(A, S)]) {
    def map[B](f: A => B): StateT[M, S, B] = StateT(s => run(s).map { case (a, s) => (f(a), s) })
    def flatMap[B](f: A => StateT[M, S, B]): StateT[M, S, B] =
      StateT(s => run(s).flatMap { case (a, s) => f(a).run(s) })
    def exec(s: S): M[S] = run(s).map(_._2)
  }

  object StateT {
    implicit def StateTInstances[M[_]: Monad, S] = new Monad[({ type l[a] = StateT[M, S, a]})#l] {
      def point[A](a: A) = StateT(s => (a, s).point[M])
      def map[A,B](ma: StateT[M, S, A])(f: A => B) = ma.map(f)
      def flatMap[A,B](ma: StateT[M, S, A])(f: A => StateT[M, S, B]) = ma.flatMap(f)
    }
    implicit def StateTLiftIO[S] = new LiftIO[({ type l[a] = StateT[IO, S, a]})#l] {
      def liftIO[A](ma: IO[A]): StateT[IO, S, A] = StateT(s => (ma.map(a => (a, s)) : IO[(A, S)]))
    }
  }

  trait Resource[A] {
    def close(a: A): Unit
  }

  object Resource {
    implicit def CloseableResource[A <: java.io.Closeable] = new Resource[A] {
      def close(a: A): Unit = a.close
    }
  }

  object RealWorld

  type IO[A] = State[RealWorld.type, A]

  object IO {
    def apply[A](a: => A): IO[A] = State[RealWorld.type,A](s => (a, s))

    def fail[A](t: Throwable): IO[A] = State(_ => throw t)

    def putStrLn(s: String): IO[Unit] = IO(Console.println(s))
  }

  implicit class IOOps[A](ma: IO[A]) {
    def liftIO[M[_]](implicit M: LiftIO[M]): M[A] = M.liftIO(ma)
    def using[B](f: A => IO[B])(implicit A: Resource[A]): IO[B] =
      ma.flatMap(a => f(a).flatMap(b => IO(A.close(a)).map(_ => b)))
    def ensuring[B](mb: IO[B]): IO[A] =
      State(rw => try { ma.run(rw) } finally { void(mb.run(rw)) } )
    def unsafePerformIO(): A = ma.run(RealWorld)._1
    def withOut(o: OutputStream): IO[A] =
      State[RealWorld.type,A](s => Console.withOut(o)(ma.run(s)))
  }

  implicit class ListOps[A](as: List[A]) {
    def traverse[M[_]: Monad, B](f: A => M[B]): M[List[B]] =
      as.foldRight(List.empty[B].point[M])((a, mlb) => f(a).flatMap(b => mlb.map(b :: _)))
  }

  implicit class BooleanOps(b: Boolean) {
    def whenM[M[_]: Monad, A](ma: M[A]): M[Unit] = if (b) ma.void else ().point[M]
    def unlessM[M[_]: Monad, A](ma: M[A]): M[Unit] = if (!b) ma.void else ().point[M]
    def fold[A](t: => A, f: => A): A = if (b) t else f
  }

  implicit val ListInstances = new Monad[List] {
    def point[A](a: A) = List(a)
    def map[A,B](ma: List[A])(f: A => B) = ma.map(f)
    def flatMap[A,B](ma: List[A])(f: A => List[B]) = ma.flatMap(f)
  }

  def void[A](a: A): Unit = ()

}
