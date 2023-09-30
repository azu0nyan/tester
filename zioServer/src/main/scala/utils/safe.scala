package utils

object safe {
  def apply(text: => String): String =
    try {
      text
    } catch
      case t: Throwable => t.toString
}
