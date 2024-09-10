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

import java.time.Instant


trait AbstractDao[T: Read : Write] {
  val schema: Schema[T]
  def tableName: String
  def jsonFields: Seq[String] = Seq()
  def jsonbFields: Seq[String] = Seq()


  lazy val fieldNames: Seq[String] = schema.asInstanceOf[zio.schema.Schema.Record[T]].fields.map(_.name)
  lazy val fieldNamesWithTable: Seq[String] = fieldNames.map(n => tableName + "." + n)
  lazy val fieldStringWithTable: String = fieldNamesWithTable.mkString(", ")
  lazy val fieldString: String = fieldNames.mkString(", ")
  //  schema.asInstanceOf[zio.schema.Schema.Record[T]].fields.head.get

  lazy val insertString = s"""INSERT INTO $tableName ($fieldString) VALUES $valuesString"""

  lazy val valuesString: String = fieldNames.map(name =>
    if (jsonFields.contains(name)) "?::json"
    else if (jsonbFields.contains(name)) "?::jsonb"
    else "?"
  ).mkString("(", ", ", ")")

  lazy val selectFragment: Fragment = Fragment.const(s"""SELECT $fieldStringWithTable FROM $tableName""")
  lazy val deleteFragment: Fragment = Fragment.const(s"""DELETE FROM $tableName""")
  lazy val updateFragment = Fragment.const(s"""UPDATE $tableName SET""")

  def insert(t: T): TranzactIO[Boolean] =
    ZIO.log(insertString).flatMap { _ =>
      tzio {
        val up = Update[T](insertString).toUpdate0(t)
        up.run
      }.map(_ == 1)

    }


  def updateWhere(set: Fragment, where: Fragment): TranzactIO[Int] = tzio {
    (updateFragment ++ set ++ fr"WHERE" ++ where).update.run
  }
  def updateWhereAnd(set: Fragment, where1: Fragment, wheres: Fragment*): TranzactIO[Int] = tzio {
    (updateFragment ++ set ++ Fragments.whereAnd(where1, wheres: _ *)).update.run
  }
  def updateWhereOr(set: Fragment, where1: Fragment, wheres: Fragment*): TranzactIO[Int] = tzio {
    (updateFragment ++ set ++ Fragments.whereOr(where1, wheres: _ *)).update.run
  }
  def updateWhereAndOpt(set: Fragment, where1: Option[Fragment], where2: Option[Fragment], wheres: Option[Fragment]*): TranzactIO[Int] = tzio {
    (updateFragment ++ set ++ Fragments.whereAndOpt(where1, where2, wheres: _ *)).update.run
  }
  def updateWhereOrOpt(set: Fragment, where1: Option[Fragment], where2: Option[Fragment], wheres: Option[Fragment]*): TranzactIO[Int] = tzio {
    (updateFragment ++ set ++ Fragments.whereOrOpt(where1, where2, wheres: _ *)).update.run
  }

  def all: TranzactIO[List[T]] = tzio(selectFragment.query[T].to[List])

  def selectWhereOption(fr: Fragment): TranzactIO[Option[T]] = tzio((selectFragment ++ fr"WHERE" ++ fr).query[T].option)
  def selectWhere(fr: Fragment): TranzactIO[T] = tzio((selectFragment ++ fr"WHERE" ++ fr).query[T].unique)
  def selectWhereList(fr: Fragment): TranzactIO[List[T]] = tzio((selectFragment ++ fr"WHERE" ++ fr).query[T].to[List])

  private def selectWhereAndQuery(fr1: Fragment, frs: Fragment*): Fragment = selectFragment ++ Fragments.whereAnd(fr1, frs: _ *)
  def selectWhereAnd(fr1: Fragment, frs: Fragment*): TranzactIO[T] = tzio(selectWhereAndQuery(fr1, frs: _ *).query[T].unique)
  def selectWhereAndOption(fr1: Fragment, frs: Fragment*): TranzactIO[Option[T]] = tzio(selectWhereAndQuery(fr1, frs: _ *).query[T].option)
  def selectWhereAndList(fr1: Fragment, frs: Fragment*): TranzactIO[List[T]] = tzio(selectWhereAndQuery(fr1, frs: _ *).query[T].to[List])

  private def selectWhereOrQuery(fr1: Fragment, frs: Fragment*): Fragment = selectFragment ++ Fragments.whereOr(fr1, frs: _ *)
  def selectWhereOr(fr1: Fragment, frs: Fragment*): TranzactIO[T] = tzio(selectWhereOrQuery(fr1, frs: _ *).query[T].unique)
  def selectWhereOrOption(fr1: Fragment, frs: Fragment*): TranzactIO[Option[T]] = tzio(selectWhereOrQuery(fr1, frs: _ *).query[T].option)
  def selectWhereOrList(fr1: Fragment, frs: Fragment*): TranzactIO[List[T]] = tzio(selectWhereOrQuery(fr1, frs: _ *).query[T].to[List])

  def selectWhereAndOptQuery(fr1: Option[Fragment], fr2: Option[Fragment], frs: Option[Fragment]*): Fragment =
    selectFragment ++ Fragments.whereAndOpt(fr1, fr2, frs: _ *)
  def selectWhereAndOpt(fr1: Option[Fragment], fr2: Option[Fragment], frs: Option[Fragment]*): TranzactIO[T] =
    tzio(selectWhereAndOptQuery(fr1, fr2, frs: _ *).query[T].unique)
  def selectWhereAndOptOption(fr1: Option[Fragment], fr2: Option[Fragment], frs: Option[Fragment]*): TranzactIO[Option[T]] =
    tzio(selectWhereAndOptQuery(fr1, fr2, frs: _ *).query[T].option)
  def selectWhereAndOptList(fr1: Option[Fragment], fr2: Option[Fragment], frs: Option[Fragment]*): TranzactIO[List[T]] =
    tzio(selectWhereAndOptQuery(fr1, fr2, frs: _ *).query[T].to[List])

  private def selectWhereOrOptQuery(fr1: Option[Fragment], fr2: Option[Fragment], frs: Option[Fragment]*): Fragment =
    selectFragment ++ Fragments.whereOrOpt(fr1, fr2, frs: _ *)
  def selectWhereOrOpt(fr1: Option[Fragment], fr2: Option[Fragment], frs: Option[Fragment]*): TranzactIO[T] =
    tzio(selectWhereOrOptQuery(fr1, fr2, frs: _ *).query[T].unique)
  def selectWhereOrOptOption(fr1: Option[Fragment], fr2: Option[Fragment], frs: Option[Fragment]*): TranzactIO[Option[T]] =
    tzio(selectWhereOrOptQuery(fr1, fr2, frs: _ *).query[T].option)
  def selectWhereOrOptList(fr1: Option[Fragment], fr2: Option[Fragment], frs: Option[Fragment]*): TranzactIO[List[T]] =
    tzio(selectWhereOrOptQuery(fr1, fr2, frs: _ *).query[T].to[List])

  def deleteWhere(fr: Fragment): TranzactIO[Int] = tzio((deleteFragment ++ fr"WHERE" ++ fr).update.run)

  private def deleteWhereAndQuery(fr1: Fragment, frs: Fragment*): Fragment = deleteFragment ++ Fragments.whereAnd(fr1, frs: _ *)
  def deleteWhereAnd(fr1: Fragment, frs: Fragment*): TranzactIO[Int] = tzio(deleteWhereAndQuery(fr1, frs: _ *).update.run)

  private def deleteWhereOrQuery(fr1: Fragment, frs: Fragment*): Fragment = deleteFragment ++ Fragments.whereOr(fr1, frs: _ *)
  def deleteWhereOr(fr1: Fragment, frs: Fragment*): TranzactIO[Int] = tzio(deleteWhereOrQuery(fr1, frs: _ *).update.run)

  private def deleteWhereAndOptQuery(fr1: Option[Fragment], fr2: Option[Fragment], frs: Option[Fragment]*): Fragment = deleteFragment ++ Fragments.whereAndOpt(fr1, fr2, frs: _ *)
  def deleteWhereAndOpt(fr1: Option[Fragment], fr2: Option[Fragment], frs: Option[Fragment]*): TranzactIO[Int] = tzio(deleteWhereAndOptQuery(fr1, fr2, frs: _ *).update.run)

  private def deleteWhereOrOptQuery(fr1: Option[Fragment], fr2: Option[Fragment], frs: Option[Fragment]*): Fragment = deleteFragment ++ Fragments.whereOrOpt(fr1, fr2, frs: _ *)
  def deleteWhereOrOpt(fr1: Option[Fragment], fr2: Option[Fragment], frs: Option[Fragment]*): TranzactIO[Int] = tzio(deleteWhereOrOptQuery(fr1, fr2, frs: _ *).update.run)

}

object AbstractDao {

  case class Ord(expr: Fragment, asc: Boolean)
  def orderByOpt(ords: Option[Ord]*): Fragment = ords.foldLeft(fr0"") {
    case (left, Some(Ord(expr, true))) => left ++ (if (left.internals.sql == "") fr0"" else fr0",") ++ expr ++ fr"ASC"
    case (left, Some(Ord(expr, false))) => left ++ (if (left.internals.sql == "") fr0"" else fr0",") ++ expr ++ fr"DESC"
    case (left, None) => left
  }

  def orderBy(ords: Ord*): Fragment = ords.foldLeft(fr0"") {
    case (left, Ord(expr, true)) => left ++ (if (left.internals.sql == "") fr0"" else fr0",") ++ expr ++ fr"ASC"
    case (left, Ord(expr, false)) => left ++ (if (left.internals.sql == "") fr0"" else fr0",") ++ expr ++ fr"DESC"
  }

  trait ById[T: Read : Write] extends AbstractDao[T] {
    def byIdOption(id: Int): TranzactIO[Option[T]] = selectWhereAndOption(fr"id = $id")
    def byId(id: Int): TranzactIO[T] = selectWhereAnd(fr"id = $id")

    def deleteById(id: Int): TranzactIO[Boolean] =
      deleteWhere(fr"id = $id").map(_ == 1)

    def updateById(id: Int, set: Fragment): TranzactIO[Boolean] =
      updateWhere(set, fr"id = $id").map(_ == 1)

    def insertReturnId(t: T): TranzactIO[Int] = tzio {
      val update = (Fragment.const(s"INSERT INTO $tableName") ++
        Fragment.const(s"($fieldString)") ++ fr"VALUES"
        ++ valuesStringDefaultIdFr(t))
        .update
      //      println(update.sql)
      update.withUniqueGeneratedKeys[Int]("id")
    }

    def toFragment(any: Any): Fragment = any match
      case i: java.time.Instant => fr"${i}"
      case i: Int => fr"${i}"
      case i: Long => fr"${i}"
      case i: String => fr"${i}"
      case i: Boolean => fr"${i}"
      case i: Double => fr"${i}"
      case i: Short => fr"${i}"
      case i@Some(x) if x.isInstanceOf[Instant] => fr"${i.asInstanceOf[Option[Instant]]}"
      case i@Some(x) if x.isInstanceOf[Int] => fr"${i.asInstanceOf[Option[Int]]}"
      case i@Some(x) if x.isInstanceOf[Long] => fr"${i.asInstanceOf[Option[Long]]}"
      case i@Some(x) if x.isInstanceOf[String] => fr"${i.asInstanceOf[Option[String]]}"
      case i@Some(x) if x.isInstanceOf[Boolean] => fr"${i.asInstanceOf[Option[Double]]}"
      case i@Some(x) if x.isInstanceOf[Double] => fr"${i.asInstanceOf[Option[Boolean]]}"
      case i@Some(x) if x.isInstanceOf[Short] => fr"${i.asInstanceOf[Option[Short]]}"
      case i@None => fr"NULL"
      case i => throw new Exception(s"Field type not supported $i")

    private def valuesStringDefaultIdFr(t: T): Fragment =
      val inner = fieldNames.zipWithIndex.map { (name, id) =>
        val value = schema.asInstanceOf[Schema.Record[T]].fields.toSeq(id).get(t)
        //      val valStr = value.toString
        //      val fr = fr"$value"
        val fr: Fragment = toFragment(value)
        if (jsonFields.contains(name)) fr ++ fr0"::json"
        else if (jsonbFields.contains(name)) fr ++ fr0"::jsonb"
        else if (name.equalsIgnoreCase("id")) fr"DEFAULT"
        else fr
      }.reduceLeft(_ ++ fr", " ++ _)
      fr"(" ++ inner ++ fr")"
    end valuesStringDefaultIdFr
  }

  trait ByAlias[T: Read] extends AbstractDao[T] {
    def byAliasOption(alias: String): TranzactIO[Option[T]] =
      selectWhereAndOption(fr"alias = $alias")

    def byAlias(alias: String): TranzactIO[T] =
      selectWhereAnd(fr"alias = $alias")

    def deleteByAlias(alias: String): TranzactIO[Boolean] =
      deleteWhere(fr"alias = $alias").map(_ == 1)

    def updateByAlias(alias: String, set: Fragment): TranzactIO[Boolean] =
      updateWhere(set, fr"alias = $alias").map(_ == 1)
  }
}
