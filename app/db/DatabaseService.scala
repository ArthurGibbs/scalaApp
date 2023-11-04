package db

import com.google.inject.Inject
import models.User

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class  DatabaseService @Inject() (databaseClient: DatabaseClient){
  def saveUser(user: User): Future[Option[User]] = {
    databaseClient.addOrUpdateUser(userRowFromData(user)).map(_.map(userDataFromRow))
  }

  def listUsers(): Future[Seq[User]] = {
    databaseClient.listUsers().map(_.map(userDataFromRow))
  }

  // CONVERTERS

  private def userDataFromRow(userRow: UserRow): User = {
    User(userRow.id, userRow.name, userRow.email, userRow.hash)
  }

  private def userRowFromData(user: User): UserRow = {
    UserRow(user.id, user.name, user.email, user.hash)
  }

}
