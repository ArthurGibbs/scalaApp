package db

import slick.jdbc.PostgresProfile.api._

class UserTable(tag: Tag) extends Table[UserRow](tag, "Users.Users") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name")
  def email = column[String]("email")
  def hash = column[String]("hash")

  def * = (id.?, name, email, hash) <> ((UserRow.apply _).tupled, UserRow.unapply)
}

case class UserRow(id: Option[Int], name: String, email: String, hash: String) {
  def withId(newId: Option[Int]): UserRow = copy(id = newId)
}

