package controllers

import com.cask.services.{AuthService, UserService}
import com.cask.{I18nSupport, Logging}
import com.google.inject.{Inject, Singleton}
import play.api.mvc._
import com.cask.WritableImplicits._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class SecureController @Inject()(val controllerComponents: ControllerComponents, userService: UserService) extends BaseController with I18nSupport with Logging {

  def test() = Action.async { implicit request: Request[AnyContent] =>
        val sessionData = AuthService.getAuthorizedUserData()(request.session)

        Future(Ok(sessionData.user.username))
  }
}
