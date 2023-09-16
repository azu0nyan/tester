package tester.srv.controller


import io.github.gaelrenoux.tranzactio.doobie.TranzactIO
import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim}
import zio.ZIO

import java.time.Clock
import scala.util.{Failure, Success}

object TokenOps {

  implicit val c: Clock = java.time.Clock.systemUTC()
  val secretKey = "someHardcodedKeyTODOAddtoConfigg" //TODO
  val algorithm = JwtAlgorithm.HS256
  val defaultTokenExpiresSeconds: Int = 48 * 60 * 60
  type Token = String

  def generateToken(userId: Int, expiresInSeconds: Int): Token = {
    val claim = JwtClaim(subject = Some(userId.toString)).issuedNow.expiresIn(expiresInSeconds)
    val token = Jwt.encode(claim, secretKey, algorithm)
    token
  }

  sealed trait ValidationResult
  case class TokenValid(id: Int) extends ValidationResult
  case object Expired extends ValidationResult
  case object CantDecode extends ValidationResult
  case object InvalidToken extends ValidationResult


  /** Returns user id if token valid */
  def decodeAndValidateUserToken(token: Token): ValidationResult = {
    Jwt.decode(token, secretKey, Seq(algorithm)) match {
      case Failure(exception) =>
        CantDecode
      case Success(claim) =>
        if (claim.isValid) //todo check
          claim.subject.flatMap(_.toIntOption) match
            case Some(id) => TokenValid(id)
            case None => CantDecode
        else Expired
    }
  }


}

