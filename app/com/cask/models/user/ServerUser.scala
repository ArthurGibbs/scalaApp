package com.cask.models.user

import play.api.libs.json.{Json, Reads, Writes}

import java.sql.ResultSet


case class ServerUser(
                       user: PersonalUser,
                       emailVerificationCode: String,
                       hash: String,
                       salt: String,
                       passwordResetCode: Option[String] = None) {

  implicit val ServerUserWrites: Writes[ServerUser] = Json.writes[ServerUser]
  implicit val ServerUserReads: Reads[ServerUser] = Json.reads[ServerUser]

}
object ServerUser {
  def fromResultSet(rs: ResultSet, u: PersonalUser): ServerUser = {
    ServerUser(
      u,
      rs.getString("email_verification_code"),
      rs.getString("hash"),
      rs.getString("salt"),
      Some(rs.getString("password_reset_code"))
    )
  }
}




