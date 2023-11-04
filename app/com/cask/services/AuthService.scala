package com.cask.services

import com.cask.Jwt
import com.cask.db.DatabaseService
import com.cask.models.User
import com.google.common.hash.Hashing
import com.google.inject.Inject
import play.api.Configuration
import play.api.libs.json.{JsNumber, JsObject, JsString, JsValue, Json}

import scala.concurrent.ExecutionContext.Implicits.global
import java.nio.charset.StandardCharsets
import scala.concurrent.Future

class AuthService @Inject() (config: Configuration, databaseService: DatabaseService, jwt: Jwt) {
  val ttl: Int = config.getOptional[Int]( "jwt.ttl").getOrElse(3600)
  val secret: String =  config.get[String]( "jwt.secret")

  def login(usernameOrEmail: String, password: String): Future[Option[(User,String)]] = {
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


            val json: JsObject = JsObject(
              Seq(
                "id"     -> JsNumber(claimedUser.id.get),
                "username" -> JsString(claimedUser.username)
              )
            )

            val token = jwt.encode(json)
            Some((claimedUser,token))
          } else {
            None
          }
        }
        case _ => None
      }
    })
  }

  lazy val serverSalt: String = config.get[String]( "app.salt")

  def getHashedPassword(rawPassword: String, salt: String): String = {
    Hashing.sha256().hashString(rawPassword+salt+serverSalt, StandardCharsets.UTF_8).toString
  }

}

