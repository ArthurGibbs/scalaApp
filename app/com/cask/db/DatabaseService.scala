package com.cask.db

import com.cask.models.User
import com.google.inject.Inject
import com.cask.db.dso.UserDSO.{DTOFromDSO, DSOFromDTO}
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

case class  DatabaseService @Inject() (databaseClient: DatabaseClient){

  def isUsernameUnused(username: String): Future[Boolean] = {
    databaseClient.isUsernameUnused(username)
  }

  def isEmailUnused(email: String): Future[Boolean] = {
    databaseClient.isEmailUnused(email)
  }

  def getUserByName(username: String): Future[Option[User]] = {
    databaseClient.getUserByName(username).map(_.map(DTOFromDSO))
  }

  def saveUser(user: User): Future[Option[User]] = {
    databaseClient.addUser(DSOFromDTO(user)).map(_.map(DTOFromDSO))
  }

  def listUsers(): Future[Seq[User]] = {
    databaseClient.listUsers().map(_.map(DTOFromDSO))
  }
}
