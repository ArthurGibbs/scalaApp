package controllers

import com.cask.{I18nSupport, Logging}
import play.api.mvc._
import com.cask.WritableImplicits._
import com.google.inject.Inject
import com.google.inject.Singleton
import com.cask.services.{AuthService, UserService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class UserController @Inject()(val controllerComponents: ControllerComponents, userService: UserService, secureAction: SecureAction, authService: AuthService) extends BaseController with I18nSupport with Logging  {

  def listUsers() = secureAction.async { implicit request: Request[AnyContent] =>

      userService.listUsers().map(users =>
        Ok(users.map(_.user.displayUser))
      )
  }

  def getUserByName(username: String) = Action.async { implicit request: Request[AnyContent] =>
      userService.getUserByName(username).map( user =>
        user match {
          case Some(user) => Ok(user.user.displayUser)
          case _ => NotFound("")
        }
      )
  }

  def getPersonalUserById() = Action.async { implicit request: Request[AnyContent] =>
    val sessionData = AuthService.verifyingUserWithRoles()(request.session)

    userService.getUserById(sessionData.user.id.get).map(maybeServerUser =>
      maybeServerUser match {
        case Some(serverUser) => Ok(serverUser.user)
        case _ => NotFound("")
      }
    )
  }
}
