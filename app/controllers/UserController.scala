package controllers

import com.cask.{I18nSupport, Logging}
import play.api.mvc._
import com.cask.WritableImplicits._
import com.cask.models.User
import com.google.inject.Inject
import com.google.inject.Singleton
import play.api.data.{Form, FormError}
import com.cask.services.UserService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class UserController @Inject()(val controllerComponents: ControllerComponents, userService: UserService) extends BaseController with I18nSupport with Logging {

  def postExample = Action.async { implicit request: Request[AnyContent] =>
    def onError(formWithErrors: Form[User]): Future[Result] = {
      val allErrors: Seq[FormError] = formWithErrors.globalErrors ++ formWithErrors.errors
      allErrors.map(_.format).distinct.foreach(x => log.warn("Registration form validation failed: " + x))
      Future(BadRequest("foo"))
    }

    def onSuccess(formDetails: User): Future[Result] = {
      log.debug(s"Received registration form for ${formDetails.name}, ${formDetails.email}")
      userService.registerUser(formDetails).map(
        Ok(_)
      )
    }

    val userRegistrationResult = User.form.bindFromRequest()
    userRegistrationResult.fold(onError, onSuccess)
  }

  def getExample() = Action.async { implicit request: Request[AnyContent] =>
    userService.listUsers().map(
      Ok(_)
    )
  }
}
