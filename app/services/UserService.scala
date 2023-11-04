package services

import db.{DatabaseService}
import com.google.inject.Inject
import models.User


class UserService @Inject() (databaseService: DatabaseService){

  def registerUser(userRegistration: User): Option[User] = {
    databaseService.saveUser(userRegistration)
  }

}
