package com.cask.db

import com.cask.models.user.ServerUser
import com.google.inject.Inject

import scala.concurrent.Future

case class  DatabaseService @Inject() (databaseClient: DatabaseClient){
  def updateUser(user: ServerUser): Future[Option[ServerUser]] = {databaseClient.updateUser(user)}
  def isUsernameUnused(username: String): Future[Boolean] = {databaseClient.isUsernameUnused(username)}
  def isEmailUnused(email: String): Future[Boolean] = {databaseClient.isEmailUnused(email)}
  def getUserByName(username: String): Future[Option[ServerUser]] = {databaseClient.getUserByName(username)}
  def getUserByEmail(email: String): Future[Option[ServerUser]]  = {databaseClient.getUserByEmail(email)}
  def getUserById(id: Int): Future[Option[ServerUser]] = {databaseClient.getUserById(id)}
  def saveUser(user: ServerUser): Future[Option[ServerUser]] = {databaseClient.addUser(user)}
  def listUsers(): Future[Seq[ServerUser]] = {databaseClient.listUsers()}
}
