package com.cask.db.dso

import com.cask.models.User
import org.joda.time.DateTime


case class UserDSO(
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
  def withId(newId: Option[Int]): UserDSO = copy(id = newId)
}

object UserDSO {
  def DTOFromDSO(dso: UserDSO): User = {
    User(
      dso.id,
      dso.username,
      dso.email,
      dso.emailVerified,
      dso.emailVerificationCode,
      dso.hash,
      dso.salt,
      dso.profileImageId,
      dso.createdOn,
      dso.lastSeen,
      dso.gender,
      dso.bio,
      dso.bioUpdated)
  }

  def DSOFromDTO(user: User): UserDSO = {
    UserDSO(
      user.id,
      user.username,
      user.email,
      user.emailVerified,
      user.emailVerificationCode,
      user.hash,
      user.salt,
      user.profileImageId,
      user.createdOn,
      user.lastSeen,
      user.gender,
      user.bio,
      user.bioUpdated)
  }
}

