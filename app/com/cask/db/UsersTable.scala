package com.cask.db

import slick.lifted.Tag
import java.sql.Timestamp
import slick.jdbc.PostgresProfile.api._

class UsersTable () {
  val users = TableQuery[UsersTable]

  class UsersTable(tag: Tag) extends Table[UserTableRow](tag, "USERS.USERS") {
    def id = column[Int]("id", O.PrimaryKey) // This is the primary key column
    def username = column[String]("username")
    def email = column[String]("email")
    def emailVerified = column[Boolean]("email_verified")
    def emailVerificationCode = column[String]("email_verification_code")
    def hash = column[String]("hash")
    def salt = column[String]("salt")
    def passwordResetCode = column[String]("password_reset_code")
    def profileImageId = column[Int]("profile_image_id")
    def createdOn = column[Timestamp]("created_on")
    def lastSeen = column[Timestamp]("last_seen")
    def gender = column[String]("gender")
    def bio = column[String]("bio")
    def bioUpdated = column[Timestamp]("bio_updated")

    // Every table needs a * projection with the same type as the table's type parameter
    def * = (id.?, username, email, emailVerified, emailVerificationCode, hash, salt, passwordResetCode.?,
      profileImageId.?, createdOn, lastSeen, gender.?, bio,bioUpdated.?) <>
      ((UserTableRow.apply _).tupled, UserTableRow.unapply)
  }

  case class UserTableRow(
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

}
