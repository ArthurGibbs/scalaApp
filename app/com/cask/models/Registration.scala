package com.cask.models

import play.api.libs.json.{Json, Reads, Writes}

case class Registration(username: String, email: String, password: String) {}

object Registration {
  implicit val RegistrationWrites: Writes[Registration] = Json.writes[Registration]
  implicit val RegistrationReads: Reads[Registration] = Json.reads[Registration]
}




