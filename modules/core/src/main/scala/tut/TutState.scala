package tut

import java.io.{File, PrintWriter}
import scala.tools.nsc.interpreter.IMain

final case class TutState(
  isCode: Boolean,
  mods: Set[Modifier],
  needsNL: Boolean,
  imain: IMain,
  pw: PrintWriter,
  spigot: Spigot,
  partial: String,
  err: Boolean,
  in: File,
  opts: List[String]
)
