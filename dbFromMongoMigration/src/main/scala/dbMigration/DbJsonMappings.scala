package dbMigration

import DbViewsShared.CourseShared
import DbViewsShared.CourseShared.AnswerStatus
import controller.UserRole.Student
import io.getquill.{JsonbValue, MappedEncoding}
import otsbridge.AnswerField.{AnswerField, TextField}
import otsbridge.CoursePiece.CourseRoot
import otsbridge.ProblemScore.{BinaryScore, ProblemScore}

import java.time.Instant

object DbJsonMappings {

  import io.circe.syntax._
  import io.circe._
  import io.circe.generic.semiauto._
  import io.circe.parser.decode


  implicit val m1 = MappedEncoding[String, JsonbValue[controller.UserRole]] { s =>
    JsonbValue(decode[controller.UserRole](s) match {
      case Left(value) =>
        Student()
      case Right(role) => role
    })
  }

  implicit val m2 = MappedEncoding[JsonbValue[controller.UserRole], String](_.value.asJson.noSpaces)

  import io.circe.generic.auto._
  import io.circe._
  import io.circe.generic.semiauto._
  import io.circe.syntax._

  implicit val fooDecoder: Decoder[ProblemScore] = deriveDecoder
  implicit val fooEncoder: Encoder[ProblemScore] = deriveEncoder

  implicit val m3 = MappedEncoding[String, JsonbValue[ProblemScore]] { s =>
    JsonbValue(decode[ProblemScore](s) match {
      case Left(value) => BinaryScore(false)
      case Right(score: ProblemScore) => score
    })
  }
  implicit val m4 = MappedEncoding[JsonbValue[ProblemScore], String](_.value.asJson.noSpaces)


  implicit val fooDecoder1: Decoder[CourseRoot] = deriveDecoder
  implicit val fooEncoder1: Encoder[CourseRoot] = deriveEncoder
  implicit val m5 = MappedEncoding[String, JsonbValue[CourseRoot]] { s =>
    JsonbValue(decode[CourseRoot](s) match {
      case Left(value) => CourseRoot("generated while mongo migrated", "generated while mongo migrated", Seq())
      case Right(root: CourseRoot) => root
    })
  }
  implicit val m6 = MappedEncoding[JsonbValue[CourseRoot], String](_.value.asJson.noSpaces)


  implicit val fooDecoder2: Decoder[AnswerField] = deriveDecoder
  implicit val fooEncoder3: Encoder[AnswerField] = deriveEncoder

  implicit val m7 = MappedEncoding[String, JsonbValue[AnswerField]] { s =>
    JsonbValue(decode[AnswerField](s) match {
      case Left(value) => TextField("generated while mongo migrated")
      case Right(score: AnswerField) => score
    })
  }
  implicit val m8 = MappedEncoding[JsonbValue[AnswerField], String](_.value.asJson.noSpaces)

  implicit val fooDecoder4: Decoder[AnswerStatus] = deriveDecoder
  implicit val fooEncoder5: Encoder[AnswerStatus] = deriveEncoder
  implicit val m9 = MappedEncoding[String, JsonbValue[AnswerStatus]] { s =>
    JsonbValue(decode[AnswerStatus](s) match {
      case Left(value) => CourseShared.Rejected(Some("Rejected while migrating to mongo, cant decode"), Instant.now())
      case Right(score: AnswerStatus) => score
    })
  }
  implicit val m10 = MappedEncoding[JsonbValue[AnswerStatus], String](_.value.asJson.noSpaces)


}
