package controllers

import com.cask.services.{AuthService, UserService}
import com.cask.{I18nSupport, Logging}
import com.google.inject.{Inject, Singleton}
import play.api.libs.json.{JsObject, JsString, JsValue, Json}
import play.api.mvc._
import com.cask.WritableImplicits._

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
              case _ => {Unauthorized("Unauthorized")}
            })

          }
          case _ => throw new IllegalStateException("Missing username and or password")
        }
      }
      case _ => throw new IllegalStateException("Body is invalid json")
    }
  }

  def logout() = Action { implicit request: Request[AnyContent] =>
    val json: JsObject = JsObject(
      Seq(
        "message"     -> JsString("Success"),
        "redirectUrl" -> JsString("/login"),
      )
    )
    Ok(json).withNewSession
  }

}
