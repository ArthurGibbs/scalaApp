package controllers

import com.google.inject.Inject

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import com.cask.Logging
import com.cask.services.AuthService
import play.api.mvc._



class SecureAction @Inject()(parser: BodyParsers.Default)(implicit ec: ExecutionContext)
  extends ActionBuilderImpl(parser) with Logging {

  override def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]): Future[Result] = {
    implicit def sessionData = AuthService.verifyingUserWithRoles()(request.session)
    block(request)
  }
}