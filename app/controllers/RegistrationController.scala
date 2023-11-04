package controllers

import com.cask.WritableImplicits._
import com.cask.models.{Registration, VerifyEmailRequest}
import com.cask.services.{AuthService, UserService}
import com.cask.{I18nSupport, Logging}
import com.google.inject.{Inject, Singleton}
import play.api.libs.json._
import play.api.mvc._
import com.cask.RequestHelper.requestToObjOrThrow

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class RegistrationController @Inject()(val controllerComponents: ControllerComponents, userService: UserService, authService: AuthService) extends BaseController with I18nSupport with Logging {

  def registerUser: Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    val registration: Registration = requestToObjOrThrow[Registration]()(request, Registration.RegistrationReads)
    log.debug(s"Received registration form for ${registration.username}, ${registration.email}")
    userService.registerUser(registration).map(maybeUser =>
      maybeUser match {
        case Some(serverUser) => Ok(serverUser.user.public)
        case _ => InternalServerError("Registration Failed")
      }
    )
  }

  def isUsernameUnused(username: String) = Action.async { implicit request: Request[AnyContent] =>
      userService.isUsernameUnused(username).map(result => {
        Ok(JsObject(
          Seq(
            "username" -> JsString(username),
            "available" -> JsBoolean(result)
          )
        ))
      })
  }

  def isEmailUnused(email: String) = Action.async { implicit request: Request[AnyContent] =>
    userService.isEmailUnused(email).map(result => {
      Ok(JsObject(
        Seq(
          "email" -> JsString(email),
          "available" -> JsBoolean(result)
        )
    ))}
    )
  }

  def verifyEmail() = Action.async { implicit request: Request[AnyContent] =>
    val verifyEmailRequest: VerifyEmailRequest = requestToObjOrThrow[VerifyEmailRequest]()(request, VerifyEmailRequest.reads)
    userService.validateEmail(verifyEmailRequest).map(user => Ok(user.user.public))
  }

}
