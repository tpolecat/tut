tutSettings

scalaVersion := sys.props("scala.version")

InputKey[Unit]("print") <<= inputTask { (argsTask: TaskKey[Seq[String]]) =>
  (argsTask, streams) map { (args, out) =>
    println(args.mkString(" "))
  }
}
