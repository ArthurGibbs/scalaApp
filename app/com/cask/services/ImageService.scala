package com.cask.services

import com.cask.db.DatabaseService
import com.cask.models.{Image, SessionData}
import com.google.inject.Inject
import org.joda.time.DateTime
import play.api.Configuration
import play.api.libs.Files
import play.api.mvc.MultipartFormData

import java.io.File
import java.util.UUID
import scala.util.matching.Regex

class  ImageService @Inject()(config: Configuration, databaseService: DatabaseService){
  val allowedExtentions = Seq("png","jpg")
  lazy val imageStore: String = config.get[String]( "images.storage.path")

  def saveImage(photo: MultipartFormData.FilePart[Files.TemporaryFile])(implicit sessionData: SessionData) = {
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

    val image: Image = Image(0, sessionData.user.id.get, file.getPath, "", true, false, DateTime.now() )
    databaseService.saveImage(image)


  }


}
