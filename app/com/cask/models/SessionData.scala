package com.cask.models

import com.cask.models.user.PublicUser
import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.{JsPath, Reads, Writes}

case class SessionData(
                        user: PublicUser,
                        roles: Seq[Role]) {}

//need to make sure this cannot exceed 4kb cookie max
object SessionData {

  implicit val SessionDataWrites: Writes[SessionData] =
    (JsPath \ "user").write[PublicUser]
      .and((JsPath \ "roles").write[Seq[Role]])(unlift(SessionData.unapply))

  implicit val SessionDataReads: Reads[SessionData] =
    (JsPath \ "user").read[PublicUser]
      .and((JsPath \ "roles").read[Seq[Role]])(SessionData.apply _)

}




