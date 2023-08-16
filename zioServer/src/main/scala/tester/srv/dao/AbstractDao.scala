package tester.srv.dao

import doobie.*
import doobie.implicits.*
import doobie.implicits.javasql.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import doobie.postgres.pgisimplicits.*
import io.github.gaelrenoux.tranzactio.doobie.{Connection, Database, TranzactIO, tzio}
import io.github.gaelrenoux.tranzactio.{DbException, doobie}
import zio.*
import zio.schema.{DeriveSchema, Schema}


trait AbstractDao[T: Read : Write] {
  val schema: Schema[T]
  def tableName: String
  def jsonFields: Seq[String] = Seq()
  def jsonbFields: Seq[String] = Seq()


  lazy val fieldNames: Seq[String] = schema.asInstanceOf[zio.schema.Schema.Record[T]].fields.map(_.name)
  lazy val fieldString: String = fieldNames.mkString(", ")


  lazy val valuesString: String = fieldNames.map(name =>
    if (jsonFields.contains(name)) "?::json"
    else if (jsonbFields.contains(name)) "?::jsonb"
    else "?"
  ).mkString("(", ", ", ")")

  lazy val selectFragment: Fragment = Fragment.const(s"""SELECT $fieldString FROM $tableName""")
  lazy val deleteFragment: Fragment = Fragment.const(s"""DELETE FROM $tableName""")
  lazy val insertString = s"""INSERT INTO $tableName ($fieldString) VALUES $valuesString"""

  def insert(t: T): TranzactIO[Int] = tzio {
    Update[T](insertString).toUpdate0(t).run
  }

  def selectWhereOption(fr: Fragment): TranzactIO[Option[T]] = tzio((selectFragment ++ fr"WHERE" ++ fr).query[T].option)
  def selectWhere(fr: Fragment): TranzactIO[T] = tzio((selectFragment ++ fr"WHERE" ++ fr).query[T].unique)
  def selectWhereList(fr: Fragment): TranzactIO[List[T]] = tzio((selectFragment ++ fr"WHERE" ++ fr).query[T].to[List])

  private def selectWhereAndQuery(frs: Fragment*): Fragment = selectFragment ++ Fragments.whereAnd(frs: _ *)
  def selectWhereAnd(frs: Fragment*): TranzactIO[T] = tzio(selectWhereAndQuery(frs: _ *).query[T].unique)
  def selectWhereAndOption(frs: Fragment*): TranzactIO[Option[T]] = tzio(selectWhereAndQuery(frs: _ *).query[T].option)
  def selectWhereAndList(frs: Fragment*): TranzactIO[List[T]] = tzio(selectWhereAndQuery(frs: _ *).query[T].to[List])

  private def selectWhereOrQuery(frs: Fragment*): Fragment = selectFragment ++ Fragments.whereOr(frs: _ *)
  def selectWhereOr(frs: Fragment*): TranzactIO[T] = tzio(selectWhereOrQuery(frs: _ *).query[T].unique)
  def selectWhereOrOption(frs: Fragment*): TranzactIO[Option[T]] = tzio(selectWhereOrQuery(frs: _ *).query[T].option)
  def selectWhereOrList(frs: Fragment*): TranzactIO[List[T]] = tzio(selectWhereOrQuery(frs: _ *).query[T].to[List])

  def selectWhereAndOptQuery(frs: Option[Fragment]*): Fragment = selectFragment ++ Fragments.whereAndOpt(frs: _ *)
  def selectWhereAndOpt(frs: Option[Fragment]*): TranzactIO[T] = tzio(selectWhereAndOptQuery(frs: _ *).query[T].unique)
  def selectWhereAndOptOption(frs: Option[Fragment]*): TranzactIO[Option[T]] = tzio(selectWhereAndOptQuery(frs: _ *).query[T].option)
  def selectWhereAndOptList(frs: Option[Fragment]*): TranzactIO[List[T]] = tzio(selectWhereAndOptQuery(frs: _ *).query[T].to[List])

  private def selectWhereOrOptQuery(frs: Option[Fragment]*): Fragment = selectFragment ++ Fragments.whereOrOpt(frs: _ *)
  def selectWhereOrOpt(frs: Option[Fragment]*): TranzactIO[T] = tzio(selectWhereOrOptQuery(frs: _ *).query[T].unique)
  def selectWhereOrOptOption(frs: Option[Fragment]*): TranzactIO[Option[T]] = tzio(selectWhereOrOptQuery(frs: _ *).query[T].option)
  def selectWhereOrOptList(frs: Option[Fragment]*): TranzactIO[List[T]] = tzio(selectWhereOrOptQuery(frs: _ *).query[T].to[List])

  def deleteWhere(fr: Fragment): TranzactIO[Int] = tzio((deleteFragment ++ fr"WHERE" ++ fr).update.run)

  private def deleteWhereAndQuery(frs: Fragment*): Fragment = deleteFragment ++ Fragments.whereAnd(frs: _ *)
  def deleteWhereAnd(frs: Fragment*): TranzactIO[Int] = tzio(deleteWhereAndQuery(frs: _ *).update.run)

  private def deleteWhereOrQuery(frs: Fragment*): Fragment = deleteFragment ++ Fragments.whereOr(frs: _ *)
  def deleteWhereOr(frs: Fragment*): TranzactIO[Int] = tzio(deleteWhereOrQuery(frs: _ *).update.run)

  private def deleteWhereAndOptQuery(frs: Option[Fragment]*): Fragment = deleteFragment ++ Fragments.whereAndOpt(frs: _ *)
  def deleteWhereAndOpt(frs: Option[Fragment]*): TranzactIO[Int] = tzio(deleteWhereAndOptQuery(frs: _ *).update.run)

  private def deleteWhereOrOptQuery(frs: Option[Fragment]*): Fragment = deleteFragment ++ Fragments.whereOrOpt(frs: _ *)
  def deleteWhereOrOpt(frs: Option[Fragment]*): TranzactIO[Int] = tzio(deleteWhereOrOptQuery(frs: _ *).update.run)

}

object AbstractDao {

  trait ById[T: Read] extends AbstractDao[T] {
    def byIdOption(id: Long): TranzactIO[Option[T]] = selectWhereAndOption(fr"id = $id")
    def byId(id: Long): TranzactIO[T] = selectWhereAnd(fr"id = $id")

    def deleteById(id: Long): TranzactIO[Int] = deleteWhere(fr"id = $id")
  }

  trait ByAlias[T: Read] extends AbstractDao[T] {
    def byAliasOption(alias: String): TranzactIO[Option[T]] = selectWhereAndOption(fr"alias = $alias")
    def byAlias(alias: String): TranzactIO[T] = selectWhereAnd(fr"alias = $alias")
    def deleteByALias(alias: String): TranzactIO[Int] = deleteWhere(fr"alias = $alias")

  }
}
