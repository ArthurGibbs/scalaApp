package com.cask.models.user

import org.joda.time.DateTime

import java.sql.Timestamp
case class ServerUserRow(
                          id: Option[Int] = None,
                          username: String,
                          email:String,
                          emailVerified: Boolean,
                          emailVerificationCode: String,
                          hash: String,
                          salt: String,
                          passwordResetCode: Option[String] = None,
                          profileImageId: Option[Int],
                          createdOn: Timestamp,
                          lastSeen: Timestamp,
                          gender: Option[String],
                          bio: String,
                          bioUpdated: Option[Timestamp]) {
}





