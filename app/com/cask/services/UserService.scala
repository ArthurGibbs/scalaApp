package com.cask.services

import com.cask.db.DatabaseService
import com.cask.models.{Registration, User}
import com.google.inject.Inject
import org.joda.time.DateTime
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future
import scala.util.Random


class UserService @Inject() (databaseService: DatabaseService, authService: AuthService){
  def isEmailUnused(email: String): Future[Boolean] = {
    databaseService.isEmailUnused(email)
  }

  def isUsernameUnused(username: String): Future[Boolean] = {
    databaseService.isUsernameUnused(username)
  }

  def getUserByName(username: String): Future[Option[User]] = {
    databaseService.getUserByName(username)
  }

  def registerUser(registration: Registration): Future[Option[User]] = {
    val result: Future[(Boolean, Boolean)] = for {
      usernameFree <- databaseService.isUsernameUnused(registration.username)
      emailFree <- databaseService.isEmailUnused(registration.username)
    } yield (usernameFree , emailFree)

    val resultingUser = result.flatMap(r => {
      if(!r._1){throw new IllegalArgumentException("username already exists")}
      if(!r._2){throw new IllegalArgumentException("email already exists")}

      val userSalt = Iterator.continually(Random.nextPrintableChar()).filter(_.isLetterOrDigit).take(64).mkString
      val emailValidationCode = Iterator.continually(Random.nextPrintableChar()).filter(_.isLetterOrDigit).take(8).mkString
      val hash = authService.getHashedPassword(registration.password, userSalt)
      val newUser = User(
        None,
        registration.username,
        registration.email,
        false,
        emailValidationCode,
        hash,
        userSalt,
        None,
        DateTime.now(),
        DateTime.now(),
        None,
        "",
        None)

        databaseService.saveUser(newUser)
    })


  resultingUser
  }

  def listUsers() : Future[Seq[User]] = {
    databaseService.listUsers()
  }

}
