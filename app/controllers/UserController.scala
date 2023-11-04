package controllers

import com.cask.{I18nSupport, Logging}
import play.api.mvc._
import com.cask.WritableImplicits._
import com.cask.models.{Registration, User}
import com.google.inject.Inject
import com.google.inject.Singleton
import play.api.data.{Form, FormError}
import com.cask.services.UserService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class UserController @Inject()(val controllerComponents: ControllerComponents, userService: UserService) extends BaseController with I18nSupport with Logging {

  def listUsers() = Action.async { implicit request: Request[AnyContent] =>
    userService.listUsers().map( users =>
      Ok(users.map(_.toDisplay()))
    )
  }

  def getUserByName(username: Option[String]) = Action.async { implicit request: Request[AnyContent] =>
    if (username.isDefined) {
      userService.getUserByName(username.get).map( user =>
        user match {
          case Some(user) => Ok(user.toDisplay())
          case _ => NotFound("")
        }
      )
    } else {
      Future(BadRequest("no username supplied"))
    }
  }
}
