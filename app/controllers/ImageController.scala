package controllers

import com.cask.services.{AuthService, UserService}
import com.cask.{I18nSupport, Logging}
import com.google.inject.{Inject, Singleton}
import play.api.mvc._
import com.cask.WritableImplicits._
import play.api.Configuration
import play.api.libs.Files

import java.io.File
import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.matching.Regex

@Singleton
class ImageController @Inject()(val controllerComponents: ControllerComponents, userService: UserService, config: Configuration) extends BaseController with I18nSupport with Logging {
  lazy val imageStore: String = config.get[String]( "images.storage.path")
  val allowedExtentions = Seq("png","jpg")
  def upload: Action[MultipartFormData[Files.TemporaryFile]] = Action.async(parse.multipartFormData) { implicit request =>
    Future {
      request.body
        .files.find(_.key == "picture")
        .map { photo =>


          val extensionPattern: Regex = "^.+\\.([^\\.]{2,5})$".r
          val extension = extensionPattern.findFirstMatchIn(photo.filename) match {
            case Some(extensionString) => {
              if (allowedExtentions.contains(extensionString.group(1).toString().toLowerCase)){
                extensionString.group(1).toLowerCase
              } else {
                throw new IllegalStateException("Invalid image extension bust be of " + allowedExtentions.toString())
              }

            }
            case None => throw new IllegalStateException("Invalid username must match ^([a-zA-Z0-9_\\-\\.]+)$")
          }

          val fileName = UUID.randomUUID.toString
          val file = new File(s"$imageStore/$fileName.$extension")
          photo.ref.moveTo(file)
          file.setReadable(true)
          file.setWritable(true)
          file.setExecutable(true)

          Ok(s"$fileName.$extension")
        }
        .getOrElse {
          BadRequest("Missing file")
        }
    }
  }
}
