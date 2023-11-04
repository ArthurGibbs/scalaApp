package com.cask.models

import play.api.libs.json.{Json, Reads, Writes}

import java.sql.ResultSet


case class ServerUser(
              user: User,
              emailVerificationCode: String,
              hash: String,
              salt: String) {

  implicit val ServerUserWrites: Writes[ServerUser] = Json.writes[ServerUser]
  implicit val ServerUserReads: Reads[ServerUser] = Json.reads[ServerUser]

}
object ServerUser {
  def fromResultSet(rs: ResultSet, u: User): ServerUser = {
    ServerUser(
      u,
      rs.getString("email_verification_code"),
      rs.getString("hash"),
      rs.getString("salt")
    )
  }
}




