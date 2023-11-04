package controllers

import com.cask.{I18nSupport, Logging}
import play.api.mvc._
import com.cask.WritableImplicits._
import com.cask.models.{PasswordResetRequest, SessionData}
import com.cask.models.user.{PersonalUser, ServerUser}
import com.google.inject.Inject
import com.google.inject.Singleton
import com.cask.services.{AuthService, UserService}
import play.api.libs.json.{JsError, JsString, JsSuccess, Json}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class UserController @Inject()(val controllerComponents: ControllerComponents, userService: UserService, secureAction: SecureAction, authService: AuthService) extends BaseController with I18nSupport with Logging  {

  def resetPasswordRequest() = Action.async { implicit request: Request[AnyContent] =>
    request.body.asJson match {
      case Some(json) => {
        val email = (json \ "email").toOption match {
          case Some(JsString(value)) => value
          case _ => throw new IllegalArgumentException("Missing email field")
        }
        userService.resetPasswordRequest(email).map(r => {
          r match {
            case Some(serverUser: ServerUser) => {Ok("Reset Email was sent")}
            case _ => throw new IllegalStateException("No user with matching details found")
          }
        })
      }
      case _ => throw new IllegalArgumentException("Invalid Json")
    }
  }

  def resetPasswordAction() = Action.async { implicit request: Request[AnyContent] =>
    request.body.asJson match {
      case Some(json) => {

        val passwordResetRequest = Json.fromJson[PasswordResetRequest](json) match {
          case JsSuccess(value, path) => value
          case JsError(errors) => throw new IllegalStateException("parsing error"+ errors.toString())
          case _ => {throw new IllegalStateException("parsing error")}
        }

        userService.resetPasswordAction(passwordResetRequest).map(r => {
          r match {
            case Some(serverUser: ServerUser) => {
              Ok(serverUser.user.public)
            }
            case _ => throw new IllegalStateException("No user with matching details found")
          }
        })
      }
      case _ => throw new IllegalArgumentException("Invalid Json")
    }
  }


  def listUsers() = secureAction.async { implicit request: Request[AnyContent] =>

      userService.listUsers().map(users =>
        Ok(users.map(_.user.public))
      )
  }

  def getUserByName(username: String) = Action.async { implicit request: Request[AnyContent] =>
      userService.getUserByName(username).map( user =>
        user match {
          case Some(user) => Ok(user.user.public)
          case _ => NotFound("")
        }
      )
  }

  def getSelf() = Action.async { implicit request: Request[AnyContent] =>
    val sessionData = AuthService.verifyingUserWithRoles()(request.session)

    userService.getUserById(sessionData.user.id.get).map(maybeServerUser =>
      maybeServerUser match {
        case Some(serverUser) => Ok(serverUser.user)
        case _ => NotFound("")
      }
    )
  }

  def setSelf() = Action.async { implicit request: Request[AnyContent] =>
    val sessionData = AuthService.verifyingUserWithRoles()(request.session)

    request.body.asJson match {
      case Some(json) => {

        val personalUser: PersonalUser = Json.fromJson[PersonalUser](json) match {
          case JsSuccess(value, path) => value
          case JsError(errors) => throw new IllegalStateException("parsing error"+ errors.toString())
          case _ => {throw new IllegalStateException("unknown error")}
        }

        if (personalUser.public.id != sessionData.user.id){
          throw new IllegalArgumentException("no!!")
        }

        userService.updateUser(personalUser).map(r => {
          r match {
            case Some(serverUser: ServerUser) => Ok(serverUser.user)
            case _ => throw new IllegalStateException("No user with matching details found")
          }
        })
      }
      case _ => throw new IllegalArgumentException("Invalid Json")
    }

    userService.getUserById(sessionData.user.id.get).map(maybeServerUser =>
      maybeServerUser match {
        case Some(serverUser) => Ok(serverUser.user)
        case _ => NotFound("")
      }
    )
  }


}
