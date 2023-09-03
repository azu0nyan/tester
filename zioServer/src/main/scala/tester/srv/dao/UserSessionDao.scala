package tester.srv.dao

import doobie.implicits.toSqlInterpolator
import AbstractDao.ById
import UserSessionDao.UserSession
import zio.schema.{DeriveSchema, Schema}
import zio.schema.{DeriveSchema, Schema}
import doobie.*
import doobie.implicits.*
import doobie.implicits.javasql.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import doobie.postgres.pgisimplicits.*
import io.github.gaelrenoux.tranzactio.DbException
import io.github.gaelrenoux.tranzactio.doobie.TranzactIO
import zio.{Task, ZIO}

import java.time.Instant

object UserSessionDao extends AbstractDao[UserSession]
  with ById[UserSession] {

  case class UserSession(id: Int, userId: Int, token: String,
                         ip: Option[String], userAgent: Option[String], platform: Option[String], locale: Option[String],
                         startAt: Instant, endAt: Instant, valid: Boolean = true)

  override val schema: Schema[UserSession] = DeriveSchema.gen[UserSession]
  override val tableName: String = "Session"


  def getValidUserSessions(id: Int): TranzactIO[List[UserSession]] =
    selectWhereAndList(fr"VALID = TRUE", fr"userId = $id")

  def invalidateSessionBySessionId(sessionId: Int): TranzactIO[Int] =
    updateWhere(fr"valid=false", fr"id = $sessionId")

  def invalidateSessionByToken(token: String): TranzactIO[Int] =
    updateWhere(fr"valid=false", fr"token = $token")

}


