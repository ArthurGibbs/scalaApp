package com.cask.models

import play.api.libs.json.{Json, Reads, Writes}

import java.sql.ResultSet

case class User(
  displayUser: DisplayUser,
  email: String,
  emailVerified: Boolean
               )

object User {

  implicit val UserWrites: Writes[User] = Json.writes[User]
  implicit val UserReads: Reads[User] = Json.reads[User]

  def fromResultSet(rs: ResultSet, du: DisplayUser): User = {
    User(
      du,
      rs.getString("email"),
      rs.getBoolean("email_verified")
    )
  }
}










