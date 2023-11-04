package com.cask.models

import play.api.libs.json.{Json, Reads, Writes}

case class SessionData(
  id: Int,
  roles: Seq[String]) {}

//need to make sure this cannot exceed 4kb cookie max
object SessionData {
  implicit val SessionDataWrites: Writes[SessionData] = Json.writes[SessionData]
  implicit val SessionDataReads: Reads[SessionData] = Json.reads[SessionData]
}




