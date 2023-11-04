package com.cask.services

import com.cask.db.DatabaseService
import com.cask.models.{Image, SessionData}
import com.google.inject.Inject
import org.joda.time.DateTime
import play.api.Configuration
import play.api.libs.Files
import play.api.mvc.{MultipartFormData, Result}

import java.io.File
import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.matching.Regex

class  ImageService @Inject()(config: Configuration, databaseService: DatabaseService){
  val allowedExtentions = Seq("png","jpg")
  lazy val imageStore: String = config.get[String]( "images.storage.path")
  val extensionPattern: Regex = "^.+\\.([^\\.]{2,5})$".r

  def saveImage(photo: MultipartFormData.FilePart[Files.TemporaryFile])(implicit sessionData: SessionData) = {
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
    val image: Image = Image(0, sessionData.user.id.get, s"$fileName.$extension", "", true, false, DateTime.now() )
    databaseService.saveImage(image)
  }

  def getImage(id: Int)(implicit sessionData: SessionData): Future[(File,String)] = {
    databaseService.getImage(id).map(mi => {
      val image = mi match {
        case Some(image: Image) => { image}
        case _ => throw new IllegalStateException("no image found with identifier")
      }
      //todo check image permissions

      val imageFile = new File(s"$imageStore/${image.path}")


      if (imageFile.exists()){

        val extension = extensionPattern.findFirstMatchIn(image.path) match {
          case Some(extensionString) => extensionString.group(1).toLowerCase
          case None => throw new IllegalStateException("how did we get here")
        }

        val contentType = extension match {
          case "jpg"| "jpeg" => { "image/jpeg"}
          case "png" => { "image/png"}
          case "gif" => { "image/gif"}
          case _ => throw new IllegalStateException("unsupported content type on disk")
        }
        (imageFile, contentType)


      } else {
        throw new IllegalStateException("file is missing from disk")
      }
    })
  }



}
