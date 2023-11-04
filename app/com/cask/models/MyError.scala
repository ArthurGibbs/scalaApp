package com.cask.models

import org.joda.time.DateTime
import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.{JsPath, Json, Writes}
case class MyError(message: String, time: DateTime = DateTime.now()) {}

object MyError {
  import com.cask.models.JodaDateTimeReadsWrites._

  implicit val MyErrorWrites: Writes[MyError] =  Json.writes[MyError]

}
