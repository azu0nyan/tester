package otsbridge

sealed trait DisplayMe

object DisplayMe {
  case object OwnPage extends DisplayMe
  case object Inline extends DisplayMe
}