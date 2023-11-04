package com.cask.models

import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.{JsPath, Reads, Writes}

case class SessionData(
              user: DisplayUser,
              roles: Seq[Role]) {}


object SessionData {

  implicit val SessionDataWrites: Writes[SessionData] =
    (JsPath \ "user").write[DisplayUser]
      .and((JsPath \ "roles").write[Seq[Role]])(unlift(SessionData.unapply))

  implicit val SessionDataReads: Reads[SessionData] =
    (JsPath \ "user").read[DisplayUser]
      .and((JsPath \ "roles").read[Seq[Role]])(SessionData.apply _)

}




