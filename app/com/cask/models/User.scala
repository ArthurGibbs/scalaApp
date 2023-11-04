package com.cask.models

import org.joda.time.DateTime
import play.api.data.Form
import play.api.data.Forms.{mapping, number, of, optional, text}
import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.{JsPath, Reads, Writes}

case class User(
              id: Option[Int] = None,
              username: String,
              email: String,
              emailVerified: Boolean,
              emailVerificationCode: String,
              hash: String,
              salt: String,
              profileImageId: Option[Int],
              createdOn: DateTime,
              lastSeen: DateTime,
              gender: Option[String],
              bio: String,
              bioUpdated: Option[DateTime]) {

  def toDisplay():DisplayUser = {
    DisplayUser(id.getOrElse(0), username, "")
  }
  def toSelfDisplay():DisplayUser = {
    DisplayUser(id.getOrElse(0), username, email)
  }
}

object User {




//  implicit val UserWrites: Writes[DisplayUser] =
//    (JsPath \ "id").write[Option[Int]]
//      .and((JsPath \ "name").write[String])
//      .and((JsPath \ "email").write[String])
//      .and((JsPath \ "hash").write[String])(unlift(User.unapply))
//
//  implicit val UserReads: Reads[DisplayUser] =
//    (JsPath \ "id").readNullable[Int]
//      .and((JsPath \ "name").read[String])
//      .and((JsPath \ "email").read[String])
//      .and((JsPath \ "hash").read[String])(User.apply _)

}




