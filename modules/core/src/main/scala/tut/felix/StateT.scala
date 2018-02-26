package tut.felix

/** State monad transformer, derived from cats.data.StateT */
final class StateT[F[_], S, A](val runF: F[S => F[(S, A)]]) extends Serializable {

  def flatMap[B](fas: A => StateT[F, S, B])(implicit F: Monad[F]): StateT[F, S, B] =
    StateT.applyF(F.map(runF) { sfsa =>
      sfsa.andThen { fsa =>
        F.flatMap(fsa) { case (s, a) =>
          fas(a).run(s)
        }
      }
    })

  def map[B](f: A => B)(implicit F: Monad[F]): StateT[F, S, B] =
    transform { case (s, a) => (s, f(a)) }

  def run(initial: S)(implicit F: Monad[F]): F[(S, A)] =
    F.flatMap(runF)(f => f(initial))

  def exec(s: S)(implicit F: Monad[F]): F[S] = F.map(run(s))(_._1)

  def runA(s: S)(implicit F: Monad[F]): F[A] = F.map(run(s))(_._2)

  def transform[B](f: (S, A) => (S, B))(implicit F: Monad[F]): StateT[F, S, B] =
    StateT.applyF(
      F.map(runF) { sfsa =>
        sfsa.andThen { fsa =>
          F.map(fsa) { case (s, a) => f(s, a) }
        }
      })

}

object StateT {

  def apply[F[_], S, A](f: S => F[(S, A)])(implicit F: Monad[F]): StateT[F, S, A] =
    new StateT(F.point(f))

  def applyF[F[_], S, A](runF: F[S => F[(S, A)]]): StateT[F, S, A] =
    new StateT(runF)

  def point[F[_], S, A](a: A)(implicit F: Monad[F]): StateT[F, S, A] =
    StateT(s => F.point((s, a)))

  def modify[F[_], S](f: S => S)(implicit F: Monad[F]): StateT[F, S, Unit] =
    StateT(s => F.point((f(s), ())))

  def get[F[_], S](implicit F: Monad[F]): StateT[F, S, S] =
    StateT(s => F.point((s, s)))

  implicit def monadStateT[F[_]: Monad, S]: Monad[({ type λ[α] = StateT[F, S, α] })#λ] =
    new Monad[({ type λ[α] = StateT[F, S, α] })#λ] {
      def point[A](a: A): StateT[F,S,A] = StateT.point(a)
      def map[A, B](fa: StateT[F,S,A])(f: A => B): StateT[F,S,B] = fa.map(f)
      def flatMap[A, B](fa: StateT[F,S,A])(f: A => StateT[F,S,B]) = fa.flatMap(f)
    }

  implicit def liftIOStateT[F[_]: Monad, S](implicit ev: LiftIO[F]): LiftIO[({ type λ[α] = StateT[F, S, α] })#λ] =
    new LiftIO[({ type λ[α] = StateT[F, S, α] })#λ] {
      def liftIO[A](ioa: IO[A]): StateT[F,S,A] =
        StateT[F, S, A](s => ev.liftIO(ioa.map((s, _))))
    }

}
