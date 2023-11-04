package db

import com.google.inject.Inject
import models.User

case class  DatabaseService @Inject() (databaseClient: DatabaseClient){
  def saveUser(user: User): Option[User] = {
    databaseClient.addOrUpdateUser(userRowFromData(user)).map(userDataFromRow)
  }

  def listUsers(): Seq[User] = {
    databaseClient.listUsers().map(userDataFromRow)
  }

  // CONVERTERS

  private def userDataFromRow(userRow: UserRow): User = {
    User(userRow.id, userRow.name, userRow.email, userRow.hash)
  }

  private def userRowFromData(user: User): UserRow = {
    UserRow(user.id, user.name, user.email, user.hash)
  }

}
