package com.cask.db

import com.google.inject.Inject
import org.joda.time.DateTime
import slick.jdbc.PostgresProfile.api._
import java.sql.Timestamp

class SlickDBClient @Inject()() extends UsersTable {
  val db = Database.forConfig("app")

  def testAdd(): Unit ={
    val foo = UserTableRow(None,"fred","",true,"","","",None,Some(12),Timestamp.valueOf(DateTime.now().toString),Timestamp.valueOf(DateTime.now().toString),None,"",None)
    db.run(users += foo)
  }

}
