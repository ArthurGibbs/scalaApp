package com.cask.models

import play.api.data.Form
import play.api.data.Forms.{mapping, text}
import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.{JsPath, Reads, Writes}

case class Registration(username: String, email: String, password: String) {}

object Registration {
  val form = Form(mapping(
    "username" -> text,
    "email" -> text,
    "password" -> text
  )(Registration.apply)(Registration.unapply))

  implicit val RegistrationWrites: Writes[Registration] =
    (JsPath \ "username").write[String]
      .and((JsPath \ "email").write[String])
      .and((JsPath \ "password").write[String])(unlift(Registration.unapply))

  implicit val RegistrationReads: Reads[Registration] =
    (JsPath \ "username").read[String]
      .and((JsPath \ "email").read[String])
      .and((JsPath \ "password").read[String])(Registration.apply _)

}




