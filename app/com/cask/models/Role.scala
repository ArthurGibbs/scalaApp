package com.cask.models

import play.api.libs.json.{Json, Reads, Writes}

case class Role(id: Int, roleName: String) {}

object Role {
  implicit val RoleWrites: Writes[Role] = Json.writes[Role]
  implicit val RoleReads: Reads[Role] = Json.reads[Role]
}






