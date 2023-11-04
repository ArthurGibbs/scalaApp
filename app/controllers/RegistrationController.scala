package controllers

import com.cask.WritableImplicits._
import com.cask.models.Registration
import com.cask.services.{AuthService, UserService}
import com.cask.{I18nSupport, Logging}
import com.google.inject.{Inject, Singleton}
import play.api.data.{Form, FormError}
import play.api.libs.json.{JsBoolean, JsObject, JsString, JsValue, Json}
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class RegistrationController @Inject()(val controllerComponents: ControllerComponents, userService: UserService, authService: AuthService) extends BaseController with I18nSupport with Logging {

  def registerUser = Action.async { implicit request: Request[AnyContent] =>
    def onError(formWithErrors: Form[Registration]): Future[Result] = {
      val allErrors: Seq[FormError] = formWithErrors.globalErrors ++ formWithErrors.errors
      val errors = allErrors.map(err => err.format + " [" + err.key + "]").distinct.map(x => {
        log.warn("Registration form validation failed: " + x)
        x
      })
      Future(BadRequest(errors))
    }

    def onSuccess(registration: Registration): Future[Result] = {
      log.debug(s"Received registration form for ${registration.username}, ${registration.email}")
      userService.registerUser(registration).map(maybeUser =>
        maybeUser match {
          case Some(user) => Ok(user.user)
          case _ => InternalServerError("Registration Failed")
        }
      )
    }

    val userRegistrationResult = Registration.form.bindFromRequest()
    userRegistrationResult.fold(onError, onSuccess)
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

  def verifyEmail(id: Int, code: String) = Action.async { implicit request: Request[AnyContent] =>
    userService.validateEmail(id, code).map(user => Ok(user.user.displayUser))
  }

}