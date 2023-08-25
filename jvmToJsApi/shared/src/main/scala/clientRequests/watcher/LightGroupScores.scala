package clientRequests.watcher

import clientRequests.watcher.LightGroupScores.UserScores
import clientRequests.{GenericRequestFailure, Route, WithToken}
import otsbridge.ProblemScore.ProblemScore
import LightGroupScores.*

object LightGroupScoresJson{

  import viewData.*
  import io.circe.syntax.*, io.circe.*, io.circe.generic.semiauto.*

  implicit val reqDec: Decoder[LightGroupScoresRequest] = deriveDecoder[LightGroupScoresRequest]
  implicit val reqEnc: Encoder[LightGroupScoresRequest] = deriveEncoder[LightGroupScoresRequest]
  implicit val resDec: Decoder[LightGroupScoresResponse] = deriveDecoder[LightGroupScoresResponse]
  implicit val resEnc: Encoder[LightGroupScoresResponse] = deriveEncoder[LightGroupScoresResponse]

}

import LightGroupScoresJson.*

object LightGroupScores extends Route[LightGroupScoresRequest, LightGroupScoresResponse] {
  type ProblemScores = Map[String, ProblemScore]
  type CourseScores = Map[String, ProblemScores]
  type UserScores = Map[String, CourseScores]
  
  
  override val route: String = "requestLightGroupScores"
}

//REQ
case class LightGroupScoresRequest(token:String, groupId:String, courseAliases:Seq[String], userIds: Seq[String]) extends WithToken




//RES
sealed trait LightGroupScoresResponse
case class LightGroupScoresSuccess(aliasToTitle: Map[String, String], userScores: UserScores) extends LightGroupScoresResponse
sealed trait LightGroupScoresFailure extends LightGroupScoresResponse
case class UnknownLightGroupScoresFailure() extends LightGroupScoresFailure

