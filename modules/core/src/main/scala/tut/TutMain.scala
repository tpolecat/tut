package tut

import java.io.File

import tut.Zed._

object TutMain {
  def main(args: Array[String]): Unit = runl(args.toList).unsafePerformIO()

  def runl(args: List[String]): IO[Unit] = {
    val (in, out) = (args(0), args(1)).umap(new File(_))
    val filter = args(2).r
    val opts   = args.drop(3)
    for {
      fa <- if (in.isFile) IO(List(in)) else FileIO.ls(in)
      ss <- fa.traverse(f => FileIO.walk(f, out, filter, opts)).map(_.flatten)
      _  <- if (ss.exists(_.err)) IO.fail(TutException("Tut execution failed.")) else IO(())
    } yield ()
  }
}
