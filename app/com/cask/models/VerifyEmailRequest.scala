package com.cask.models

import play.api.libs.json.{Json, Reads, Writes}

case class VerifyEmailRequest(id: Int, code: String)

object VerifyEmailRequest {
  implicit val writes: Writes[VerifyEmailRequest] = Json.writes[VerifyEmailRequest]
  implicit val reads: Reads[VerifyEmailRequest] = Json.reads[VerifyEmailRequest]
}



