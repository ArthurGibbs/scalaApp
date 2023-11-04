package controllers

import com.cask.models.user.{PersonalUser, PublicUser, ServerUser}
import com.google.inject.Inject
import com.google.inject.Singleton
import org.joda.time.DateTime
import play.api._
import play.api.mvc._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(val controllerComponents: ControllerComponents, config: Configuration) extends BaseController {



  lazy val webUrl: String = config.get[String]( "frontendUrl")

  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }

  def verifyEmail() = Action { implicit request: Request[AnyContent] =>
   val user = ServerUser(PersonalUser(PublicUser(Some(45),"example user",Some(345),DateTime.now(), DateTime.now(), Option("male"),"", None),"asdasd@asdasd.asd", true),"123123","123123123","123123123",None)
    Ok(views.html.email.template_registration(user, webUrl))
  }
}
