package models

import play.api.data.Form
import play.api.data.Forms.{mapping, text}
import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json.{JsPath, Reads, Writes}

case class UserRegistration(name: String, email: String, hash: String)

object UserRegistration {
  val form = Form(mapping(
    "name" -> text,
    "email" -> text,
    "hash" -> text
  )(UserRegistration.apply)(UserRegistration.unapply))

  implicit val UserRegistrationWrites: Writes[UserRegistration] =
    (JsPath \ "name").write[String]
      .and((JsPath \ "email").write[String])
      .and((JsPath \ "hash").write[String])(unlift(UserRegistration.unapply))

  implicit val UserRegistrationReads: Reads[UserRegistration] =
    (JsPath \ "name").read[String]
      .and((JsPath \ "email").read[String])
      .and((JsPath \ "hash").read[String])(UserRegistration.apply _)
}




