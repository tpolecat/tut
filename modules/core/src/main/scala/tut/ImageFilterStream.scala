package tut

import java.io.{OutputStream, FilterOutputStream}
import java.nio.charset.StandardCharsets
import java.util.Base64

class ImageFilterStream(os: OutputStream) extends FilterOutputStream(os) {
  import ImageFilterStream._

  val F: State = State(_ => F)

  val T_B64: State = State(_ => T_B64)
  val T_URL: State = State(_ => T_URL)

  val S: State = State {
    case 27 => I0
    case _  => F
  }

  val I0: State = State {
    case ']' => I1
    case _   => F
  }

  val I1: State = State {
    case '1' => I2
    case _   => F
  }

  val I2: State = State {
    case '3' => I3
    case _   => F
  }

  val I3: State = State {
    case '3' => I4
    case _   => F
  }

  val I4: State = State {
    case '7' => I_B64
    case '8' => I_URL
    case _   => F
  }

  val I_B64: State = State {
    case '\u0007' => T_B64
    case _        => I_B64
  }

  val I_URL: State = State {
    case '\u0007' => T_URL
    case _        => I_URL
  }

  def get(n: Int): Option[Entry] =
    unsafeEntries.drop(n).headOption

  def length: Int = unsafeEntries.length

  private var unsafeEntries: List[Entry] = Nil
  private var stack: List[Int] = Nil
  private var state: State     = S // Start

  private def decodeDataString(input: List[Int]): String =
    new String(
      stack.reverse.drop(6).map(_.toByte).toArray,
      StandardCharsets.UTF_8)

  private def parseKeyValuePairs(input: String): (Map[String, String], Option[String]) = {
    val index = input.lastIndexOf(':')
    val (input0, extra) = if (index > 0) {
      val (prefix, suffix) = input.splitAt(index)
      prefix -> Some(suffix.drop(1))
    } else input -> None

    input0.split(";")
      .flatMap(_.split("=", 2).toList match {
        case head :: tail :: Nil => Some(head -> tail)
        case _                   => None
      })
      .toMap -> extra
  }

  override def write(n: Int): Unit =
    state.apply(n) match {

      case F =>
        stack.foldRight(())((c, _) => super.write(c))
        super.write(n)
        stack = Nil
        state = S

      case T_URL =>
        val (map, extra) = parseKeyValuePairs(decodeDataString(stack))
        val maybeEntry = map.get("url").map(url =>
          Entry.URL(url, map.get("alt")))
        unsafeEntries = maybeEntry.toList ::: unsafeEntries
        stack.foldRight(())((c, _) => super.write(c))
        super.write(n)
        stack = Nil
        state = S

      case T_B64 =>
        val (map, extra) = parseKeyValuePairs(decodeDataString(stack))
        val maybeEntry =
          for {
            _ <- map.get("File")
            data <- extra
          } yield Entry.Base64(data, map.get("alt"))
        unsafeEntries = maybeEntry.toList ::: unsafeEntries
        stack.foldRight(())((c, _) => super.write(c))
        super.write(n)
        stack = Nil
        state = S

      case s =>
        stack = n :: stack
        state = s

    }

}

object ImageFilterStream {

  private lazy val decoder: Base64.Decoder = Base64.getDecoder

  final case class State(apply: Int => State)

  sealed trait Entry
  object Entry {
    final case class URL(url: String, alt: Option[String]) extends Entry
    final case class Base64(data: String, alt: Option[String]) extends Entry {
      lazy val decoded: Array[Byte] = decoder.decode(data)

      lazy val fileExtension: String = decoded.take(8).toList match {
        case List(0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A) => "png"
        case List(0xFF, 0xD8, 0xFF, 0xDB | 0xE0 | 0xE1, _, _, _, _) => "jpg"
        case List(0x47, 0x49, 0x46, 0x38, 0x37 | 0x39, 0x61, _, _) => "gif"
        case List(0x42, 0x4D, _, _, _, _, _, _) => "bmp"
        case _ => "img" // ¯\_(ツ)_/¯
      }
    }
  }
}
