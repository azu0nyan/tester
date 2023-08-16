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


  lazy val valuesString = fieldNames.map(name =>
    if (jsonFields.contains(name)) "?::json"
    else if (jsonbFields.contains(name)) "?::jsonb"
    else "?"
  ).mkString("(", ", ", ")")

  lazy val select = Fragment.const(s"""SELECT $fieldString FROM $tableName""")
  lazy val insertString = s"""INSERT INTO $tableName ($fieldString) VALUES $valuesString"""

  def insert(t: T) = tzio {
    Update[T](insertString).toUpdate0(t).run
  }

}

trait ById[T: Read] extends AbstractDao[T] {
  def byId(id: Long): TranzactIO[Option[T]] = tzio {
    (select ++ fr"""WHERE id = $id""").query[T].option
  }
}

trait ByAlias[T: Read] extends AbstractDao[T] {
  def byAlias(alias: String): TranzactIO[Option[T]] = tzio {
    (select ++ fr"""WHERE alias = $alias""").query[T].option
  }
}
