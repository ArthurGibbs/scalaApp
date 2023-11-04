package com.cask.models

import play.api.libs.json.{Json, Reads, Writes}

case class PasswordResetRequest(email: String)

object PasswordResetRequest {
  implicit val passwordResetRequestWrites: Writes[PasswordResetRequest] = Json.writes[PasswordResetRequest]
  implicit val passwordResetRequestReads: Reads[PasswordResetRequest] = Json.reads[PasswordResetRequest]
}
