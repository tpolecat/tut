package tut.felix

trait LiftIO[F[_]] {
  def liftIO[A](ioa: IO[A]): F[A]
}
