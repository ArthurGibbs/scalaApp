package com.cask.models.user

import org.joda.time.DateTime
import play.api.libs.json.{Json, Reads, Writes}
import com.cask.models.JodaDateTimeReadsWrites._
import java.sql.ResultSet

case class PublicUser(
  id: Option[Int] = None,
  username: String,
  profileImageId: Option[Int],
  createdOn: DateTime,
  lastSeen: DateTime,
  gender: Option[String],
  bio: String,
  bioUpdated: Option[DateTime]) {}

object PublicUser {
  implicit val DisplayUserWrites: Writes[PublicUser] = Json.writes[PublicUser]
  implicit val DisplayUserReads: Reads[PublicUser] = Json.reads[PublicUser]

  def fromResultSet(rs: ResultSet): PublicUser = {
    PublicUser(
      Some(rs.getInt("id")),
      rs.getString("username"),
      Some(rs.getInt("profile_image_id")),
      new DateTime(rs.getTimestamp("created_on")),
      new DateTime(rs.getTimestamp("last_seen")),
      Some(rs.getString("gender")),
      rs.getString("bio"),
      Some(new DateTime(rs.getTimestamp("bio_updated")))
    )
  }
}








