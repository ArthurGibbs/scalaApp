package com.cask.db.rows

import com.cask.models.User

case class UserRow(id: Option[Int], name: String, email: String, hash: String) {
  def withId(newId: Option[Int]): UserRow = copy(id = newId)
}

object UserRow {
  def userDataFromRow(userRow: UserRow): User = {
    User(userRow.id, userRow.name, userRow.email, userRow.hash)
  }

  def userRowFromData(user: User): UserRow = {
    UserRow(user.id, user.name, user.email, user.hash)
  }
}

