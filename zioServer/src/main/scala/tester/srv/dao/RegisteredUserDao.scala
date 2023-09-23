package tester.srv.dao


import io.github.gaelrenoux.tranzactio.doobie.{TranzactIO, tzio}
import CourseTemplateProblemDao.CourseTemplateProblem
import zio.schema.{DeriveSchema, Schema}
import doobie.*
import doobie.implicits.*
import doobie.implicits.javasql.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import doobie.postgres.pgisimplicits.*
import RegisteredUserDao.RegisteredUser
import AbstractDao.{ById, Ord}
import clientRequests.admin.UserList.{UserFilter, UserOrder}

import java.time.Instant


object RegisteredUserDao extends AbstractDao[RegisteredUser]
  with ById[RegisteredUser] {
  case class RegisteredUser(id: Int, login: String, firstName: String, lastName: String, email: String,
                            passwordHash: String, passwordSalt: String, registeredAt: Instant) {
    def toViewData: viewData.UserViewData = viewData.UserViewData(id.toString, login,
      Some(firstName), Some(lastName), Some(email), Seq(), registeredAt)
  }

  override val schema: Schema[RegisteredUser] = DeriveSchema.gen[RegisteredUser]
  override val tableName: String = "RegisteredUser"

  def byLogin(login: String): TranzactIO[Option[RegisteredUser]] =
    selectWhereOption(fr"login ILIKE ${login}")


  def loginExists(login: String): TranzactIO[Boolean] =
    case class Exists(exists: Boolean)
    tzio {
      sql"""SELECT EXISTS(SELECT * FROM RegisteredUser where login ILIKE ${login})"""
        .query[Exists].unique.map(_.exists)
    }

  def filterToFrag(f: UserFilter): Fragment = f match
    case UserFilter.MatchesRegex(regex) => fr"(login ILIKE $regex OR firstName ILIKE $regex OR lastName ILIKE $regex OR email ILIKE $regex)"
    case UserFilter.Teacher => ???
    case UserFilter.User => ???
    case UserFilter.Admin => ???
    case UserFilter.Watcher => ???

  def orderToFrag(o: UserOrder): Ord = o match
    case UserOrder.ByDateRegistered(asc) => Ord(fr"registeredAt", asc)
    case UserOrder.ByLogin(asc) => Ord(fr"login", asc)
    case UserOrder.ByFirstName(asc) => Ord(fr"firstName", asc)
    case UserOrder.ByLastName(asc) => Ord(fr"lastName", asc)
    case UserOrder.ByEmail(asc) => Ord(fr"email", asc)

  def byFilterInOrder(filters: Seq[UserFilter], order: Seq[UserOrder],
                      itemsPerPage: Int, page: Int): TranzactIO[Seq[RegisteredUser]] = tzio {
    val fragment = selectFragment ++ fr"WHERE" ++ Fragments.and(filters.map(filterToFrag): _ *) ++
      fr"ORDER BY" ++ AbstractDao.orderBy(order.map(orderToFrag): _ *) ++
      Fragment.const(s"LIMIT $itemsPerPage OFFSET ${itemsPerPage * page}")
    println(fragment)

    fragment
      .query[RegisteredUser].to[List]
  }

}

