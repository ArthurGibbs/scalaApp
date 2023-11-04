package controllers

import com.cask.RequestHelper.requestToObjOrThrow
import com.cask.{I18nSupport, Logging}
import play.api.mvc._
import com.cask.WritableImplicits._
import com.cask.models.{PasswordResetAction, PasswordResetRequest, Registration, SessionData}
import com.cask.models.user.{PersonalUser, ServerUser}
import com.google.inject.Inject
import com.google.inject.Singleton
import com.cask.services.{AuthService, UserService}
import play.api.libs.json.{JsError, JsString, JsSuccess, Json}

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class UserController @Inject()(val controllerComponents: ControllerComponents, userService: UserService, secureAction: SecureAction, authService: AuthService) extends BaseController with I18nSupport with Logging  {

  def resetPasswordRequest() = Action.async { implicit request: Request[AnyContent] =>
    val passwordResetRequest: PasswordResetRequest = requestToObjOrThrow[PasswordResetRequest]()(request, PasswordResetRequest.passwordResetRequestReads)
    userService.resetPasswordRequest(passwordResetRequest.email).map(r => {
      r match {
        case Some(serverUser: ServerUser) => {Ok("Reset Email was sent")}
        case _ => throw new IllegalStateException("No user with matching details found")
      }
    })
  }
  def resetPasswordAction() = Action.async { implicit request: Request[AnyContent] =>
    val passwordResetAction: PasswordResetAction = requestToObjOrThrow[PasswordResetAction]()(request, PasswordResetAction.passwordResetActionReads)
    userService.resetPasswordAction(passwordResetAction).map(r => {
      r match {
        case Some(serverUser: ServerUser) => {
          Ok(serverUser.user.public)
        }
        case _ => throw new IllegalStateException("No user with matching details found")
      }
    })
  }

  def listUsers() = secureAction.async { implicit request: Request[AnyContent] =>
      AuthService.verifyingUserWithRoles()(request.session)

      userService.listUsers().map(users =>
        Ok(users.map(_.user.public))
      )
  }
  def getUserByName(username: String) = Action.async { implicit request: Request[AnyContent] =>
      AuthService.verifyingUserWithRoles()(request.session)

      userService.getUserByName(username).map( user =>
        user match {
          case Some(user) => Ok(user.user.public)
          case _ => NotFound("")
        }
      )
  }

  def getSelf() = Action.async { implicit request: Request[AnyContent] =>
    val sessionData = AuthService.verifyingUserWithRoles()(request.session)

    userService.getUserById(sessionData.id).map(maybeServerUser =>
      maybeServerUser match {
        case Some(serverUser) => Ok(serverUser.user)
        case _ => NotFound("")
      }
    )
  }
  def setSelf() = Action.async { implicit request: Request[AnyContent] =>
    val personalUser: PersonalUser = requestToObjOrThrow[PersonalUser]()(request, PersonalUser.UserReads)
    val sessionData = AuthService.verifyingUserWithRoles()(request.session)

    if (personalUser.public.id.get != sessionData.id){
      throw new IllegalArgumentException("no!!")
    }

    userService.updateUser(personalUser).map(r => {
      r match {
        case Some(serverUser: ServerUser) => Ok(serverUser.user)
        case _ => throw new IllegalStateException("No user with matching details found")
      }
    })
  }

}
