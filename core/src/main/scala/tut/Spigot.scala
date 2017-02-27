package tut

import java.io.{ByteArrayOutputStream, FilterOutputStream, OutputStream}

import tut.Zed._

class Spigot(os: OutputStream) extends FilterOutputStream(os) {
  private var baos = new ByteArrayOutputStream()
  def bytes = baos.toByteArray

  private[this] var active = true
  private[this] def ifActive(f: => Unit): Unit = if (active) f
  def setActive(b: Boolean): IO[Unit] = IO { baos.reset(); active = b }

  private[this] var replInput: Option[String] = None
  def commentAfter(text: String): IO[Unit] = IO { replInput = Some(text) }
  def stopCommenting(): IO[Unit] = IO { replInput = None }
  private[this] var output = new StringBuilder()
  private[this] def comment(): Unit = "// ".map(_.toInt).foreach(write)
  private[this] var wasNL: Boolean = false

  override def write(n: Int): Unit = {
    val commenting: Boolean = replInput.exists(output.indexOf(_) != 1)
    if (wasNL && commenting) { wasNL = false; comment() }
    output.append(n.toChar)
    baos.write(n); ifActive(super.write(n))
    wasNL = (n == '\n'.toInt)
   }
}
