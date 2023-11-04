package controllers

import com.duenna.{I18nSupport, Logging}
import models.UserRegistration

import javax.inject._
import play.api.mvc._
import com.duenna.WritableImplicits._
import play.api.data.{Form, FormError}

@Singleton
class UserController @Inject()(val controllerComponents: ControllerComponents) extends BaseController with I18nSupport with Logging {

  def postExample = Action { implicit request: Request[AnyContent] =>
    def onError(formWithErrors: Form[UserRegistration]): Result = {
      val allErrors: Seq[FormError] = formWithErrors.globalErrors ++ formWithErrors.errors
      allErrors.map(_.format).distinct.foreach(x => log.warn("Registration form validation failed: " + x))
      BadRequest("foo")
    }

    def onSuccess(formDetails: UserRegistration): Result = {
      log.debug(s"Received registration form for ${formDetails.name}, ${formDetails.email}")
      Ok(formDetails)
    }

    val userRegistrationResult = UserRegistration.form.bindFromRequest()
    userRegistrationResult.fold(onError, onSuccess)
  }

  def getExample() = Action { implicit request: Request[AnyContent] =>
    val user1 : UserRegistration = new UserRegistration("ian", "ian@gmail.com", "123")
    val user2 : UserRegistration = new UserRegistration("asdfasdf", "asdfasdf@gmail.com", "1233123")
    val list: List[UserRegistration] = List(user1, user2)
    Ok(list)
  }
}