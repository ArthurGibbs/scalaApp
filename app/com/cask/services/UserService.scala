package com.cask.services

import com.cask.db.DatabaseService
import com.cask.models.user.{PersonalUser, PublicUser, ServerUser}
import com.cask.models.Registration
import com.google.inject.Inject
import org.joda.time.DateTime

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Random
import scala.util.matching.Regex


class UserService @Inject() (databaseService: DatabaseService, authService: AuthService, emailUtil: EmailUtil){
  def validateEmail(id: Int, code: String): Future[ServerUser] = {
    databaseService.getUserById(id).flatMap( mu => mu match {
      case Some(serverUser) => {
        if(serverUser.emailVerificationCode == code){
          val updatedUser = serverUser.copy(user = serverUser.user.copy(emailVerified = true))

          databaseService.updateUser(updatedUser).map(mu => mu match {
            case Some(user) => user
            case _ => {throw new IllegalArgumentException("error updating user")}
          })
        } else {
          throw new IllegalArgumentException("Code does not match")
        }
      }
      case _ => {
        throw new IllegalArgumentException("User not found")
      }
    })
  }

  def isEmailUnused(email: String): Future[Boolean] = {
    databaseService.isEmailUnused(email)
  }
  def isUsernameUnused(username: String): Future[Boolean] = {
    databaseService.isUsernameUnused(username)
  }

  def getUserByName(username: String): Future[Option[ServerUser]] = {
    databaseService.getUserByName(username)
  }
  def getUserById(id: Int): Future[Option[ServerUser]] = {
    databaseService.getUserById(id)
  }
  def getUserByEmail(email: String): Future[Option[ServerUser]] = {
    databaseService.getUserByEmail(email)
  }


  def registerUser(registration: Registration): Future[Option[ServerUser]] = {
    val emailPattern: Regex = "^([a-zA-Z0-9_\\-\\.]+)@([a-zA-Z0-9_\\-\\.]+)\\.([a-zA-Z]{2,63})$".r
    emailPattern.findFirstMatchIn(registration.email) match {
      case Some(_) => {}
      case None => throw new IllegalStateException("Invalid Email")
    }

    val usernamePattern: Regex = "^([a-zA-Z0-9_\\-\\.]+)$".r
    usernamePattern.findFirstMatchIn(registration.username) match {
      case Some(_) => {}
      case None => throw new IllegalStateException("Invalid username must match ^([a-zA-Z0-9_\\-\\.]+)$")
    }

    val result: Future[(Boolean, Boolean)] = for {
      usernameFree <- databaseService.isUsernameUnused(registration.username)
      emailFree <- databaseService.isEmailUnused(registration.username)
    } yield (usernameFree , emailFree)

    val resultingUser = result.flatMap(r => {
      if(!r._1){throw new IllegalArgumentException("username already exists")}
      if(!r._2){throw new IllegalArgumentException("email already exists")}
      //todo add more validation

      val userSalt = Iterator.continually(Random.nextPrintableChar()).filter(_.isLetterOrDigit).take(64).mkString
      val emailValidationCode = Iterator.continually(Random.nextPrintableChar()).filter(_.isLetterOrDigit).take(8).mkString
      val hash = authService.getHashedPassword(registration.password, userSalt)
      val du = PublicUser(
        None,
        registration.username,
        None,
        DateTime.now(),
        DateTime.now(),
        None,
        "",
        None)

      val user = PersonalUser(du, registration.email, emailVerified = false)
      val newUser = ServerUser(user,
        emailValidationCode,
        hash,
        userSalt)

        databaseService.saveUser(newUser).map(maybeNewUser => {
          maybeNewUser match {
            case Some(newUser) => {
              emailUtil.sendMail("arthurgibbs@gmail.com", "has it worked", views.html.template_registration(newUser).toString())
              maybeNewUser
            }
            case _ => throw new IllegalStateException("saving to database failed")
          }

        })
    })


  resultingUser
  }

  def listUsers() : Future[Seq[ServerUser]] = {
    databaseService.listUsers()
  }

}
