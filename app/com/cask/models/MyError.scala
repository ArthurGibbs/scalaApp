package com.cask.models

import com.cask.models.JodaDateTimeReadsWrites.{jodaDateWrites}
import org.joda.time.DateTime
import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.{JsPath, Writes}
case class MyError(message: String, time: DateTime = DateTime.now()) {}

object MyError {
    implicit val MyErrorWrites: Writes[MyError] =
      (JsPath \ "error").write[String]
        .and((JsPath \ "time").write[DateTime](jodaDateWrites))(unlift(MyError.unapply))
}
