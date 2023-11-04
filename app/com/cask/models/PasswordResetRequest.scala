package com.cask.models

import com.cask.models.user.PersonalUser
import play.api.libs.json.{Json, Reads, Writes}

case class PasswordResetRequest(id: Int, code: String, password: String)

object PasswordResetRequest {
  implicit val UserWrites: Writes[PasswordResetRequest] = Json.writes[PasswordResetRequest]
  implicit val UserReads: Reads[PasswordResetRequest] = Json.reads[PasswordResetRequest]
}
