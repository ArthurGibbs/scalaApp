package com.cask.services

import com.cask.db.DatabaseService
import com.cask.models.User
import com.google.inject.Inject

import scala.concurrent.Future


class UserService @Inject() (databaseService: DatabaseService){

  def registerUser(userRegistration: User): Future[Option[User]] = {
    databaseService.saveUser(userRegistration)
  }

  def listUsers() : Future[Seq[User]] = {
    databaseService.listUsers()
  }

}
