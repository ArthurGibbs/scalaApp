package com.cask

import com.cask.WritableImplicits.jsonWritable
import com.cask.errors.RedirectingUnauthorizedException
import com.cask.models.MyError
import play.api.http.HttpErrorHandler
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.{JsObject, JsPath, JsString, Json, Reads, Writes}
import play.api.mvc.Results._
import play.api.mvc._

import javax.inject.Singleton
import scala.concurrent._

@Singleton
class ErrorHandler extends HttpErrorHandler {
  def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {
    Future.successful(
      Status(statusCode)("A client error occurred: " + message)
    )
  }

  def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = {
    Future.successful(
      result = exception match {
        case e: RedirectingUnauthorizedException => {
          val json: JsObject = JsObject(
            Seq(
              "message"     -> JsString(e.getMessage()),
              "redirectUrl" -> JsString(e.getRedirect()),
            )
          )

          Unauthorized(json)

        }
        case _ => {
          InternalServerError(MyError(message = exception.getMessage))
        }
      }
    )
  }
}