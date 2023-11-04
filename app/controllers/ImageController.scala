package controllers

import akka.stream.scaladsl.{FileIO, Source}
import akka.util.ByteString
import com.cask.services.{AuthService, ImageService, UserService}
import com.cask.{I18nSupport, Logging}
import com.google.inject.{Inject, Singleton}
import play.api.mvc._
import com.cask.WritableImplicits._
import com.cask.models.Image
import play.api.Configuration
import play.api.http.HttpEntity
import play.api.libs.Files
import play.api.libs.json.Json

import java.io.File
import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.matching.Regex

@Singleton
class ImageController @Inject()(val controllerComponents: ControllerComponents, authService: AuthService, imageService: ImageService) extends BaseController with I18nSupport with Logging {
  def upload: Action[MultipartFormData[Files.TemporaryFile]] = Action.async(parse.multipartFormData) { implicit request =>
    implicit val sessionData = AuthService.verifyingUserWithRoles()(request.session)
    request.body.files.find(_.key == "picture").map { photo =>
      imageService.saveImage(photo).map(oi => {
        oi match {
          case Some(i) => Ok( i.publicView())
          case _ => throw new IllegalStateException("error saving image")
        }
      })
    }.getOrElse {
      Future(BadRequest("Missing file"))
    }
  }

  def get(id: String) = Action.async { implicit request =>
    implicit val sessionData = AuthService.verifyingUserWithRoles()(request.session)
    imageService.getImage(id.toInt).map(imageFile => {
      val path: java.nio.file.Path      = imageFile._1.toPath
      val source: Source[ByteString, _] = FileIO.fromPath(path)

      val contentLength = Some(imageFile._1.length())
      Result(
        header = ResponseHeader(200, Map.empty),
        body = HttpEntity.Streamed(source, contentLength, Some(imageFile._2))
      )

    })


  }
}
