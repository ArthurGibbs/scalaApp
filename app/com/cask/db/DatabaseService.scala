package com.cask.db

import com.cask.models.User
import com.google.inject.Inject
import com.cask.db.dso.UserDSO.{DTOFromDSO, DSOFromDTO}
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

case class  DatabaseService @Inject() (databaseClient: DatabaseClient){

  def updateUser(user: User): Future[Option[User]] = {
    databaseClient.updateUser(DSOFromDTO(user)).map(_.map(DTOFromDSO))
  }

  def isUsernameUnused(username: String): Future[Boolean] = {
    databaseClient.isUsernameUnused(username)
  }

  def isEmailUnused(email: String): Future[Boolean] = {
    databaseClient.isEmailUnused(email)
  }

  def getUserByName(username: String): Future[Option[User]] = {
    databaseClient.getUserByName(username).map(_.map(DTOFromDSO))
  }
  def getUserByEmail(email: String): Future[Option[User]]  = {
    databaseClient.getUserByEmail(email).map(_.map(DTOFromDSO))
  }
  def getUserById(id: Int): Future[Option[User]] = {
    databaseClient.getUserById(id).map(_.map(DTOFromDSO))
  }

  def saveUser(user: User): Future[Option[User]] = {
    databaseClient.addUser(DSOFromDTO(user)).map(_.map(DTOFromDSO))
  }

  def listUsers(): Future[Seq[User]] = {
    var mook = databaseClient.listUsers()
    mook.map(_.map(DTOFromDSO))
  }
}
