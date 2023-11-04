package com.cask.models.user

import play.api.libs.json.{Json, Reads, Writes}

import java.sql.ResultSet

case class PersonalUser(
                         public: PublicUser,
                         email: String,
                         emailVerified: Boolean
               )

object PersonalUser {
  implicit val UserWrites: Writes[PersonalUser] = Json.writes[PersonalUser]
  implicit val UserReads: Reads[PersonalUser] = Json.reads[PersonalUser]

  def fromResultSet(rs: ResultSet, du: PublicUser): PersonalUser = {
    PersonalUser(
      du,
      rs.getString("email"),
      rs.getBoolean("email_verified")
    )
  }
}










