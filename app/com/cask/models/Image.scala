package com.cask.models

import com.cask.models.user.PublicUser
import org.joda.time.DateTime
import play.api.libs.json.{JsObject, Json, Reads, Writes}
import com.cask.models.JodaDateTimeReadsWrites._
import com.fasterxml.jackson.annotation.JsonValue

import java.sql.ResultSet

case class Image(
                  id: Int,
                  userId: Int,
                  path: String,
                  caption: String,
                  public: Boolean,
                  hidden: Boolean,
                  uploaded: DateTime) {


  def publicView(): JsObject = {
    val jsObject = Json.toJson(this).as[JsObject]
    jsObject - "path"
  }
}

object Image {

  def fromResultSet(rs: ResultSet): Image = {
    Image(
      rs.getInt("id"),
      rs.getInt("user_id"),
      rs.getString("path"),
      rs.getString("caption"),
      rs.getBoolean("public"),
      rs.getBoolean("hidden"),
      new DateTime(rs.getTimestamp("uploaded")))
  }

  def getFieldList(): Seq[String] = {
    Seq("id","user_id","path","caption","public","hidden", "uploaded")
  }
}
