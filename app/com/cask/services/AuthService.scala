package com.cask.services

import com.cask.Jwt
import com.cask.db.DatabaseService
import com.cask.models.User
import com.google.common.hash.Hashing
import com.google.inject.Inject
import play.api.Configuration

import scala.concurrent.ExecutionContext.Implicits.global
import java.nio.charset.StandardCharsets
import scala.concurrent.Future

class AuthService @Inject() (config: Configuration, databaseService: DatabaseService) {
  def login(usernameOrEmail: String, password: String): Future[Option[String]] = {
    val tuple = for {
      maybeNameUser <- databaseService.getUserByName(usernameOrEmail)
      maybeEmailUser <- databaseService.getUserByEmail(usernameOrEmail)
    } yield (maybeNameUser, maybeEmailUser)

    tuple.map(atuple => {
      val user = atuple match {
        case (Some(user), None) => {user}
        case (None, Some(user)) => {user}
        case _ => { throw new IllegalStateException(" not found")}
      }
      if (getHashedPassword(password, user.salt) == user.hash) {
        val ttl: Int = config.getOptional[Int]( "jwt.ttl").getOrElse(3600)
        val secret: String =  config.get[String]( "jwt.secret")
        val jwt = Jwt(Map(
          "id"     -> user.id,
          "username" -> user.username
        ), secret , ttl)
        Some(jwt)
      } else {
        None
      }
    })

  }

  lazy val serverSalt: String = config.get[String]( "app.salt")

  def getHashedPassword(rawPassword: String, salt: String): String = {
    Hashing.sha256().hashString(rawPassword+salt+serverSalt, StandardCharsets.UTF_8).toString
  }

}

