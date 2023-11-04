package com.cask.models

import org.joda.time.DateTime
import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json.{JsPath, Reads, Writes}

import java.sql.ResultSet
import com.cask.models.JodaDateTimeReadsWrites.{jodaDateReads, jodaDateWrites}

case class DisplayUser(
  id: Option[Int] = None,
  username: String,
  profileImageId: Option[Int],
  createdOn: DateTime,
  lastSeen: DateTime,
  gender: Option[String],
  bio: String,
  bioUpdated: Option[DateTime]) {}

object DisplayUser {

  implicit val DisplayUserWrites: Writes[DisplayUser] =
    (JsPath \ "id").writeNullable[Int]
      .and((JsPath \ "username").write[String])
      .and((JsPath \ "profileImageId").writeNullable[Int])
      .and((JsPath \ "createdOn").write[DateTime])
      .and((JsPath \ "lastSeen").write[DateTime])
      .and((JsPath \ "gender").writeNullable[String])
      .and((JsPath \ "bio").write[String])
      .and((JsPath \ "bioUpdated").writeNullable[DateTime])(unlift(DisplayUser.unapply))

  implicit val DisplayUserReads: Reads[DisplayUser] =
    (JsPath \ "id").readNullable[Int]
      .and((JsPath \ "username").read[String])
      .and((JsPath \ "profileImageId").readNullable[Int])
      .and((JsPath \ "createdOn").read[DateTime])
      .and((JsPath \ "lastSeen").read[DateTime])
      .and((JsPath \ "gender").readNullable[String])
      .and((JsPath \ "bio").read[String])
      .and((JsPath \ "bioUpdated").readNullable[DateTime])(DisplayUser.apply _)

  def fromResultSet(rs: ResultSet): DisplayUser = {
    DisplayUser(
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








