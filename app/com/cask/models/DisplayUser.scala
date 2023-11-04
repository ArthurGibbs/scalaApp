package com.cask.models

import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.{JsPath, Reads, Writes}

case class DisplayUser(id: Int, name: String, email: String) {}

object DisplayUser {

  implicit val DisplayUserWrites: Writes[DisplayUser] =
    (JsPath \ "id").write[Int]
      .and((JsPath \ "name").write[String])
      .and((JsPath \ "hash").write[String])(unlift(DisplayUser.unapply))

  implicit val DisplayUserReads: Reads[DisplayUser] =
    (JsPath \ "id").read[Int]
      .and((JsPath \ "name").read[String])
      .and((JsPath \ "hash").read[String])(DisplayUser.apply _)

}








