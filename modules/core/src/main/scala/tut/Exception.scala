package tut

final case class TutException(msg: String) extends RuntimeException(msg)
