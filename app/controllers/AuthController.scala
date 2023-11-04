package controllers

import com.cask.services.{AuthService, UserService}
import com.cask.{I18nSupport, Logging}
import com.google.inject.{Inject, Singleton}
import play.api.libs.json.{JsObject, JsString, JsSuccess, JsValue, Json}
import play.api.mvc._
import com.cask.WritableImplicits._
import com.cask.errors.RedirectingUnauthorizedException
import com.cask.models.SessionData

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class AuthController @Inject()(val controllerComponents: ControllerComponents, userService: UserService, authService: AuthService) extends BaseController with I18nSupport with Logging {

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
              case _ => {throw new RedirectingUnauthorizedException("Invalid username/email and or password","")}
            })

          }
          case _ => throw new IllegalStateException("Missing username and or password")
        }
      }
      case _ => throw new IllegalStateException("Invalid json")
    }
  }

  def session(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    val session: SessionData = AuthService.verifyingUserWithRoles()(request.session)
    Ok(session)
  }

  def logout() = Action { implicit request: Request[AnyContent] =>
    Ok(JsObject(
      Seq(
        "message" -> JsString("Success"),
        "redirectUrl" -> JsString("/login"),
      )
    )).withNewSession
  }
}
