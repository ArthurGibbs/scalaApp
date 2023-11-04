package controllers

import com.cask.WritableImplicits._
import com.cask.models.{DisplayUser, Registration, User}
import com.cask.services.{AuthService, UserService}
import com.cask.{I18nSupport, Logging}
import com.google.inject.{Inject, Singleton}
import play.api.data.{Form, FormError}
import play.api.libs.json.{JsObject, JsString, JsValue, Json}
import play.api.mvc._
import com.cask.WritableImplicits._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class AuthController @Inject()(val controllerComponents: ControllerComponents, userService: UserService, authService: AuthService) extends BaseController with I18nSupport with Logging {

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
          case Some(user) => Ok(user.toSelfDisplay())
          case _ => InternalServerError("Registration Failed")
        }
      )
    }

    val userRegistrationResult = Registration.form.bindFromRequest()
    userRegistrationResult.fold(onError, onSuccess)
  }

  def isUsernameUnused(username: String) = Action.async { implicit request: Request[AnyContent] =>
      userService.isUsernameUnused(username).map(result => Ok(result))
  }

  def isEmailUnused(email: String) = Action.async { implicit request: Request[AnyContent] =>
    userService.isEmailUnused(email).map(result => Ok(result))
  }

  def verifyEmail(id: Int, code: String) = Action.async { implicit request: Request[AnyContent] =>
    userService.validateEmail(id, code).map(user => Ok(user.toDisplay()))
  }

  def login() = Action.async { implicit request: Request[AnyContent] =>
    val maybeJson: Option[JsValue] = request.body.asJson
    maybeJson match {
      case Some(json) => {
        val maybeUsernameOrEmail = (json \ "usernameOrEmail").toOption.flatMap(jsv => jsv match {
          case JsString(usernameOrEmail) => Some(usernameOrEmail)
          case _ => None
        })
        val maybePassword = (json \ "password").toOption.flatMap(jsv => jsv match {
          case JsString(password) => Some(password)
          case _ => None
        })
        (maybeUsernameOrEmail, maybePassword) match {
          case (Some(usernameOrEmail),  Some(password)) => {
            authService.login(usernameOrEmail, password).map(mt => mt match {
              case Some(sessionData) => {
                Ok(sessionData)
                  .withSession(request.session + (AuthService.SESSIONDATAKEY -> Json.stringify(Json.toJson(sessionData))))
              }
              case _ => {Unauthorized("Unauthorized")}
            })

          }
          case _ => throw new IllegalStateException("Missing username and or password")
        }
      }
      case _ => throw new IllegalStateException("Body is invalid json")
    }
  }
}
