package com.cask.services

import com.cask.Jwt
import com.cask.db.DatabaseService
import com.cask.models.{SessionData, User}
import com.google.common.hash.Hashing
import com.google.inject.Inject
import play.api.Configuration
import play.api.libs.json.{JsNumber, JsObject, JsString, JsSuccess, JsValue, Json}
import play.api.mvc.{AnyContent, Request, Session}

import scala.concurrent.ExecutionContext.Implicits.global
import java.nio.charset.StandardCharsets
import scala.concurrent.Future

class AuthService @Inject() (config: Configuration, databaseService: DatabaseService, jwt: Jwt) {
  val ttl: Int = config.getOptional[Int]( "jwt.ttl").getOrElse(3600)
  val secret: String =  config.get[String]( "jwt.secret")
  lazy val serverSalt: String = config.get[String]( "app.salt")

  def login(usernameOrEmail: String, password: String): Future[Option[SessionData]] = {
    val tuple = for {
      maybeNameUser <- databaseService.getUserByName(usernameOrEmail)
      maybeEmailUser <- databaseService.getUserByEmail(usernameOrEmail)
    } yield (maybeNameUser, maybeEmailUser)

    tuple.map(atuple => {
      val maybeClaimedUser = atuple match {
        case (Some(user), None) => {Some(user)}
        case (None, Some(user)) => {Some(user)}
        case _ => { None}
      }
      maybeClaimedUser match {
        case Some(claimedUser) => {
          if (getHashedPassword(password, claimedUser.salt) == claimedUser.hash) {

            //todo get roles
            val roles = Seq()

            Some((SessionData(claimedUser.toDisplay(), roles)))
          } else {
            None
          }
        }
        case _ => None
      }
    })
  }



  def getHashedPassword(rawPassword: String, salt: String): String = {
    Hashing.sha256().hashString(rawPassword+salt+serverSalt, StandardCharsets.UTF_8).toString
  }
}

object AuthService {
  val SESSIONDATAKEY = "userSession"

  def getAuthorizedUserData()(session : Session) = {
    val sessionData = session.get(AuthService.SESSIONDATAKEY) match {
      case Some(serializedString) => {
        val jsValue = Json.parse(serializedString)
        val sessionData = Json.fromJson[SessionData](jsValue) match {
          case JsSuccess(value, path) => value
          case _ => {throw new IllegalStateException("parsing error")}
        }
        //decode[SessionData](serializedString)

        sessionData
      }
      case _ => {
        throw new IllegalStateException("UNATHORIZED TODO")
        //maybe redirecting error as this should only be non logged in users
      }
    }
    sessionData
  }
}

