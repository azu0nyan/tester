package controller

import io.circe.generic.auto._
import io.circe._
import io.circe.generic.semiauto._
import io.circe.syntax._

sealed trait UserRole

object UserRole {
  implicit val fooDecoder: Decoder[UserRole] = deriveDecoder
  implicit val fooEncoder: Encoder[UserRole] = deriveEncoder

  case class Student() extends UserRole
  case class LtiUser(userId:String, consumerKey: String) extends UserRole
  case class Teacher() extends UserRole
  case class Watcher() extends UserRole
  case class Admin() extends UserRole
}
