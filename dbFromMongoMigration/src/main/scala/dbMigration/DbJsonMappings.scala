package dbMigration

import controller.UserRole.Student
import io.getquill.{JsonbValue, MappedEncoding}

object DbJsonMappings {

  import io.circe.syntax._
  import io.circe._
  import io.circe.generic.semiauto._
  import io.circe.parser.decode


  implicit val me = MappedEncoding[String, JsonbValue[controller.UserRole]] {
    s =>

      JsonbValue(decode[controller.UserRole](s) match {
        case Left(value) =>
          println(s)
          Student()
        case Right(role) => role
      })
  }

  implicit val md = MappedEncoding[JsonbValue[controller.UserRole], String](_.value.asJson.noSpaces)

}
