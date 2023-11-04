package com.cask.services

import akka.actor.ActorSystem
import com.cask.db.DatabaseService
import com.cask.errors.RedirectingUnauthorizedException
import com.cask.models.user.{PersonalUser, PublicUser, ServerUser}
import com.cask.models.{PasswordResetAction, Registration, VerifyEmailRequest}
import com.google.inject.Inject
import org.joda.time.DateTime
import play.api.Configuration

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Random
import scala.util.matching.Regex


class UserService @Inject() (databaseService: DatabaseService, authService: AuthService, emailUtil: EmailUtil, config: Configuration, actorSystem: ActorSystem){
  lazy val webUrl: String = config.get[String]( "frontendUrl")

  def validateEmail(verifyEmailRequest: VerifyEmailRequest): Future[ServerUser] = {
    databaseService.getUserById(verifyEmailRequest.id).flatMap( mu => mu match {
      case Some(serverUser) => {
        if(serverUser.emailVerificationCode == verifyEmailRequest.code){
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
  def updateUser(updateRequestPersonalUser: PersonalUser): Future[Option[ServerUser]] = {
    databaseService.getUserById(updateRequestPersonalUser.public.id.get).map(mu => {
      mu match {
        case Some(serverUser: ServerUser) => {
          val updatedUser = serverUser.copy(
            user = serverUser.user.copy(
              public = serverUser.user.public.copy(
                gender = updateRequestPersonalUser.public.gender,
                bio = updateRequestPersonalUser.public.bio,
                profileImageId = updateRequestPersonalUser.public.profileImageId
              )
            )
          )
          databaseService.updateUser(updatedUser).map(maybeUpdated => {
            maybeUpdated match {
              case Some(serverUser: ServerUser) => { Some(serverUser)}
              case _ => throw new IllegalStateException("error updating database")
            }
          })

        }
        case _ => throw new IllegalStateException("No user found with id")
      }
    }).flatten
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

      val userSalt = generateRandomUserSalt
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
              Future {
                emailUtil.sendMail("arthurgibbs@gmail.com", "Get started", views.html.email.template_registration(newUser, webUrl).toString())
              }

              maybeNewUser
            }
            case _ => throw new IllegalStateException("saving to database failed")
          }

        })
    })
  resultingUser
  }
  def resetPasswordRequest(email: String): Future[Option[ServerUser]] = {
    databaseService.getUserByEmail(email).map(mu => mu match {
      case Some(serverUser: ServerUser) =>{
        val passwordResetCode = Iterator.continually(Random.nextPrintableChar()).filter(_.isLetterOrDigit).take(8).mkString
        val alteredUser = serverUser.copy(passwordResetCode = Some(passwordResetCode))

        databaseService.updateUser(alteredUser).map(mu => mu match {
          case Some(user) => {
            Future {
              emailUtil.sendMail("arthurgibbs@gmail.com", "Password Reset", views.html.email.template_passwordReset(user, webUrl).toString())
            }
            Some(user)
          }
          case _ => throw new IllegalArgumentException("error updating user")
        })

      }
      case None => throw new IllegalStateException("No user found with matching email")
    }).flatten
  }
  def resetPasswordAction(passwordResetAction: PasswordResetAction) = {
    databaseService.getUserById(passwordResetAction.id).map(mu => mu match {
      case Some(serverUser: ServerUser) =>{
        val isMatch = serverUser.passwordResetCode match {
          case Some(code) => code == passwordResetAction.code
          case _ => false
        }
        if (isMatch) {
          val newSalt = generateRandomUserSalt
          val newHash = authService.getHashedPassword(passwordResetAction.password, newSalt)
          val alteredUser = serverUser.copy(passwordResetCode = None, salt = newSalt, hash = newHash)

          databaseService.updateUser(alteredUser).map(mu => mu match {
            case Some(user) => {
              Future {
                emailUtil.sendMail("arthurgibbs@gmail.com", "Password Reset", views.html.email.template_passwordChange(user, webUrl).toString())
              }
              Some(user)
            }
            case _ => {throw new IllegalArgumentException("error updating user")}
          })


        } else {
          throw new RedirectingUnauthorizedException("invalid request", "")
        }
      }
      case None => throw new IllegalStateException("No user found with matching id")
    }).flatten
  }
  def listUsers() : Future[Seq[ServerUser]] = {
    databaseService.listUsers()
  }

  //--------------------------------------------------------------------------------------------------------------------

  private def generateRandomUserSalt = {
    Iterator.continually(Random.nextPrintableChar()).filter(_.isLetterOrDigit).take(64).mkString
  }
}
