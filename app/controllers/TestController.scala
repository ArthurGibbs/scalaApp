package controllers

import com.cask.{I18nSupport, Logging}
import com.google.inject.{Inject, Singleton}
import play.api.mvc._
import com.cask.db.{ SlickDBClient}

@Singleton
class TestController @Inject()(val controllerComponents: ControllerComponents, slickDBClient: SlickDBClient) extends BaseController with I18nSupport with Logging {
  def create: Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    slickDBClient.testAdd()
    Ok("ok")
  }
}
