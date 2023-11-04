package db


case class UserRow(id: Option[Int], name: String, email: String, hash: String) {
  def withId(newId: Option[Int]): UserRow = copy(id = newId)
}

