package controllers

import com.cask.{I18nSupport, Logging}
import models.User
import play.api.mvc._
import com.cask.WritableImplicits._
import com.google.inject.Inject
import play.api.data.{Form, FormError}
import services.UserService

import javax.inject.Singleton
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class UserController @Inject()(val controllerComponents: ControllerComponents, userService: UserService) extends BaseController with I18nSupport with Logging {

  def postExample = Action { implicit request: Request[AnyContent] =>
    def onError(formWithErrors: Form[User]): Result = {
      val allErrors: Seq[FormError] = formWithErrors.globalErrors ++ formWithErrors.errors
      allErrors.map(_.format).distinct.foreach(x => log.warn("Registration form validation failed: " + x))
      BadRequest("foo")
    }

    def onSuccess(formDetails: User): Result = {
      log.debug(s"Received registration form for ${formDetails.name}, ${formDetails.email}")
      val result = userService.registerUser(formDetails)
      Ok(result)
    }

    val userRegistrationResult = User.form.bindFromRequest()
    userRegistrationResult.fold(onError, onSuccess)
  }

  def getExample() = Action.async { implicit request: Request[AnyContent] =>
    val user1 : User = new User(name= "ian", email = "ian@gmail.com", hash ="123")
    val user2 : User = new User(name= "asdfasdf", email = "asdfasdf@gmail.com", hash ="1233123")
    val list: List[User] = List(user1, user2)
    Future(Ok(list))
  }
}
