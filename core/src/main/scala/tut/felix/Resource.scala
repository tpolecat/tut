package tut.felix

trait Resource[A] {
  def close(a: A): IO[Unit]
}

object Resource {
  implicit def CloseableResource[A <: java.io.Closeable] =
    new Resource[A] {
      def close(a: A) = IO(a.close)
    }
}
