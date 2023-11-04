package com.cask.services

import com.cask.db.DatabaseService
import com.cask.models.{Registration, User}
import com.google.inject.Inject
import org.joda.time.DateTime
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future
import scala.util.Random


class UserService @Inject() (databaseService: DatabaseService, authService: AuthService, emailUtil: EmailUtil){
  def validateEmail(id: Int, code: String): Future[User] = {
    databaseService.getUserById(id).flatMap( mu => mu match {
      case Some(user) => {
        if(user.emailVerificationCode == code){
          val updatedUser = user.copy(emailVerified = true)

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

  def getUserByName(username: String): Future[Option[User]] = {
    databaseService.getUserByName(username)
  }
  def getUserById(id: Int): Future[Option[User]] = {
    databaseService.getUserById(id)
  }
  def getUserByEmail(email: String): Future[Option[User]] = {
    databaseService.getUserByEmail(email)
  }


  def registerUser(registration: Registration): Future[Option[User]] = {
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

  def listUsers() : Future[Seq[User]] = {
    databaseService.listUsers()
  }

}
