package services

import db.DatabaseService
import com.google.inject.Inject
import models.User

import scala.concurrent.Future


class UserService @Inject() (databaseService: DatabaseService){

  def registerUser(userRegistration: User): Future[Option[User]] = {
    databaseService.saveUser(userRegistration)
  }

  def listUsers() : Future[Seq[User]] = {
    databaseService.listUsers()
  }

}
