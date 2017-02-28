package tut

import java.io.File

import scala.tools.nsc.interpreter.Results

import tut.FileIO.Encoding
import tut.Zed._

object Tut {
  val state: Tut[TutState] = State.get.lift[IO]

  def mod(f: TutState => TutState): Tut[Unit] = State.modify(f).lift[IO]

  def file(in: File): Tut[Unit] = for {
    lines <- FileIO.lines(in).liftIO[Tut]
    _     <- lines.zipWithIndex.traverse { case (l, num) => line(l, num + 1) }
  } yield ()

  // Private, utility methods

  private def line(text: String, lineNumber: Int): Tut[Unit] =
    for {
      s <- Tut.state
      inv = s.mods.filter(m => m == Invisible || m.isInstanceOf[Decorate])
      _ <- checkBoundary(text, "```", false, Set())
      s <- Tut.state
      mods = modifiers(text)
      _ <- IO {
        if (mods(Reset)) {
          s.imain.reset()
          s.imain.settings.processArguments(s.opts, true)
          s
        } else {
          s
        }
      }.liftIO[Tut]
      _ <- s.isCode.fold(interp(text, lineNumber), out(fixShed(text, mods ++ inv)))
      _ <- checkBoundary(text, "```tut", true, mods)
    } yield ()

  private def interp(text: String, lineNum: Int): Tut[Unit] =
    Tut.state >>= { s =>
      (text.trim.nonEmpty || s.partial.nonEmpty || s.mods(Silent)).whenM[Tut,Unit] {
        for {
          _ <- s.needsNL.whenM(out(""))
          _ <- (s.mods(Invisible)).unlessM(out(prompt(s) + text))
          _ <- s.spigot.setActive(!(s.mods(Silent) || (s.mods(Invisible)))).liftIO[Tut]
          _ <- s.mods(Book).whenM(s.spigot.commentAfter(s.partial + "\n" + text).liftIO[Tut])
          r <- IO(s.imain.interpret(s.partial + "\n" + text)).liftIO[Tut] >>= {
            case Results.Incomplete => incomplete(text)
            case Results.Success    => if (s.mods(Fail)) error(lineNum, Some("failure was asserted but no failure occurred")) else success
            case Results.Error      => if (s.mods(NoFail) || s.mods(Fail)) success else error(lineNum)
          }
          _ <- s.mods(Book).whenM(s.spigot.stopCommenting().liftIO[Tut])
          _ <- s.spigot.setActive(true).liftIO[Tut]
          _ <- IO(s.pw.flush).liftIO[Tut]
        } yield ()
      }
    }

  private def checkBoundary(text: String, find: String, code: Boolean, mods: Set[Modifier]): Tut[Unit] =
    (text.trim.startsWith(find)).whenM(Tut.mod(s => s.copy(isCode = code, needsNL = false, mods = mods)))

  private def fixShed(text: String, mods: Set[Modifier]): String = {
    val decorationMods = mods.filter(_.isInstanceOf[Decorate])
    if (mods(Invisible)) {
      ""
    } else if (text.startsWith("```tut")) {
      if (mods(Plain) || mods(Evaluated)) "```" else "```scala"
    } else {
      if (text.startsWith("```") && decorationMods.nonEmpty) {
        val decorations = decorationMods map { case m: Decorate =>
          m.decoration
        case _ => ""
        } mkString " "
        s"""$text
           |{: $decorations }""".stripMargin
      }
      else
        text
    }
  }

  private def modifiers(text: String): Set[Modifier] =
    if (text.startsWith("```tut:"))
      text.split(":").toList.tail.map(Modifier.unsafeFromString).toSet
    else
      Set.empty

  private def out(text: String): Tut[Unit] =
    for {
      s <- Tut.state
      _ <- (s.mods(Invisible) || s.mods(Evaluated)).unlessM(IO { s.pw.println(text); s.pw.flush() }.liftIO[Tut])
    } yield ()

  private def success: Tut[Unit] =
    for {
      s <- Tut.state
      _ <- new String(s.spigot.bytes, Encoding).endsWith("...").whenM(out("")) // #29
      _ <- Tut.mod(s => s.copy(needsNL = !s.mods(Silent), partial = ""))
    } yield ()

  private def incomplete(s: String): Tut[Unit] =
    Tut.mod(a => a.copy(partial = a.partial + "\n" + s, needsNL = false))

  private def error(n: Int, msg: Option[String] = None): Tut[Unit] =
    for {
      s <- Tut.state
      _ <- Tut.mod(_.copy(err = true))
      _ <- IO(Console.err.println(f"[tut] *** Error reported at ${s.in.getCanonicalPath}%s:$n%d${msg.fold("")(": " + _)}%s")).liftIO[Tut]
      _ <- IO(Console.err.write(s.spigot.bytes)).liftIO[Tut]
    } yield ()

  private def prompt(s: TutState): String =
    if (s.mods(Silent) || s.mods(Book) || s.mods(Evaluated)) ""
    else if (s.partial.isEmpty) "scala> "
    else                        "     | "
}
