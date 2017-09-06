package tut

// A stub of the old Zed micro-library, just to let us replace it without disturbing other code.
object Zed extends tut.felix.Syntax {

  type IO[A]              = tut.felix.IO[A]
  type Monad[F[_]]        = tut.felix.Monad[F]
  type StateT[F[_], S, A] = tut.felix.StateT[F, S, A]
  type LiftIO[F[_]]       = tut.felix.LiftIO[F]
  type Resource[A]        = tut.felix.Resource[A]

  val IO     = tut.felix.IO
  val StateT = tut.felix.StateT

  object State {
    def get[A] = new GetPartial[A]
    class GetPartial[A] {
      def lift[F[_]: Monad]: StateT[F, A, A] = StateT.get[F, A]
    }
    def modify[A](f: A => A) = new ModifyPartial[A](f)
    class ModifyPartial[A](f: A => A) {
      def lift[F[_]: Monad]: StateT[F, A, Unit] = StateT.modify[F, A](f)
    }
  }

}
