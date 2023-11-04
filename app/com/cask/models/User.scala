package com.cask.models

import org.joda.time.DateTime

case class User(
              id: Option[Int] = None,
              username: String,
              email: String,
              emailVerified: Boolean,
              emailVerificationCode: String,
              hash: String,
              salt: String,
              profileImageId: Option[Int],
              createdOn: DateTime,
              lastSeen: DateTime,
              gender: Option[String],
              bio: String,
              bioUpdated: Option[DateTime]) {

  def toDisplay():DisplayUser = {
    DisplayUser(id.getOrElse(0), username, "")
  }
  def toSelfDisplay():DisplayUser = {
    DisplayUser(id.getOrElse(0), username, email)
  }
}




