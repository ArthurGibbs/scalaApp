package com.cask.db

import com.cask.models.User
import com.google.inject.Inject
import com.cask.db.rows.UserRow.{userDataFromRow, userRowFromData}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class  DatabaseService @Inject() (databaseClient: DatabaseClient){
  def saveUser(user: User): Future[Option[User]] = {
    databaseClient.addOrUpdateUser(userRowFromData(user)).map(_.map(userDataFromRow))
  }

  def listUsers(): Future[Seq[User]] = {
    databaseClient.listUsers().map(_.map(userDataFromRow))
  }
}
