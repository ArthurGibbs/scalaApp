package com.cask.models

import play.api.libs.json.{Json, Reads, Writes}

case class PasswordResetAction(id: Int, code: String, password: String)

object PasswordResetAction {
  implicit val passwordResetActionWrites: Writes[PasswordResetAction] = Json.writes[PasswordResetAction]
  implicit val passwordResetActionReads: Reads[PasswordResetAction] = Json.reads[PasswordResetAction]
}
