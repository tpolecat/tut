package tut

import scala.util.matching.Regex

sealed abstract class Modifier extends Product with Serializable

final case object NoFail                        extends Modifier
final case object Fail                          extends Modifier
final case object Silent                        extends Modifier
final case object Book                          extends Modifier
final case object Plain                         extends Modifier
final case object Invisible                     extends Modifier
final case object Evaluated                     extends Modifier
final case class  Decorate(decoration: String)  extends Modifier
final case object Reset                         extends Modifier

object Modifier {
  private val DecorateP: Regex = "decorate\\((.*)\\)".r

  def fromString(s: String): Option[Modifier] = Some(s) collect {
    case "nofail"               => NoFail
    case "fail"                 => Fail
    case "silent"               => Silent
    case "book"                 => Book
    case "plain"                => Plain
    case "invisible"            => Invisible
    case "evaluated"            => Evaluated
    case DecorateP(decoration)  => Decorate(decoration)
    case "reset"                => Reset
  }

  def unsafeFromString(s: String): Modifier =
    fromString(s).getOrElse(throw TutException("No such modifier: " + s))
}
