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

  def insert(t: T): TranzactIO[Boolean] = tzio {
    val up = Update[T](insertString).toUpdate0(t)
    println(insertString)
    up.run
  }.map(_ == 1)


  def updateWhere(set: Fragment, where: Fragment): TranzactIO[Int] = tzio {
    (updateFragment ++ set ++ fr"WHERE" ++ where).update.run
  }
  def updateWhereAnd(set: Fragment, where: Fragment*): TranzactIO[Int] = tzio {
    (updateFragment ++ set ++ Fragments.whereAnd(where: _ *)).update.run
  }
  def updateWhereOr(set: Fragment, where: Fragment*): TranzactIO[Int] = tzio {
    (updateFragment ++ set ++ Fragments.whereOr(where: _ *)).update.run
  }
  def updateWhereAndOpt(set: Fragment, where: Option[Fragment]*): TranzactIO[Int] = tzio {
    (updateFragment ++ set ++ Fragments.whereAndOpt(where: _ *)).update.run
  }
  def updateWhereOrOpt(set: Fragment, where: Option[Fragment]*): TranzactIO[Int] = tzio {
    (updateFragment ++ set ++ Fragments.whereOrOpt(where: _ *)).update.run
  }

  def all: TranzactIO[List[T]] = tzio(selectFragment.query[T].to[List])

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
      println(update.sql)
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
