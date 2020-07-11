package controller.db

import java.time.{Clock, Instant, ZonedDateTime}

import controller.PasswordHashingSalting
import org.mongodb.scala._
import org.bson.types.ObjectId
import org.mongodb.scala.model.Filters._

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import org.mongodb.scala.model.Updates._

object User {
  def apply(login: String, passwordHash: String, passwordSalt: String, firstName: Option[String], lsatName: Option[String], email: Option[String],
            registeredAt: Option[Instant], lastLogin: Option[Instant]): User =
    new User(new ObjectId(), login, passwordHash, passwordSalt, firstName, lsatName, email, registeredAt, lastLogin)

  /** blocking */
  def exists(login: String): Boolean = byLogin(login).nonEmpty


  /** blocking */
  def byLogin(login:String):Option[User] = users.byField("login", login)//Await.result(users.find(equal("login", login)).first().headOption(), Duration.Inf)

  /** blocking */
  def checkPassword(user:User, password:String):Boolean = PasswordHashingSalting.checkPassword(password, user.passwordHash, user.passwordSalt)


}

case class User(_id: ObjectId,
                login: String,
                passwordHash: String,
                passwordSalt: String,
                firstName: Option[String] = None,
                lastName: Option[String] = None,
                email: Option[String] = None,
                registeredAt: Option[Instant],
                lastLogin: Option[Instant])  extends MongoObject{
  def  idAndLoginStr = s"[${_id} - $login]"

  def updateLastLogin():User = {
    users.updateField(this, "lastLogin", Clock.systemUTC().instant())
    users.byId(this._id).get
  }

  def groups:Seq[Group] = UserToGroup.userGroups(this)
}
