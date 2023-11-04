package com.cask.db

import com.google.inject.Inject
import org.joda.time.DateTime
import slick.jdbc.PostgresProfile.api._
import java.sql.Timestamp
import scala.util.Random

class SlickDBClient @Inject()() extends UsersTable {
  val db = Database.forConfig("app")

  def testAdd(): Unit ={
    val foo = UserTableRow(None,randomString,randomString,true,"2342","234234","234234",None,Some(12),new Timestamp(DateTime.now().getMillis()),new Timestamp(DateTime.now().getMillis()),None,"",None)
    db.run(addUsersQuery.insertOrUpdate(foo))
  }

  def randomString():String = {
    Iterator.continually(Random.nextPrintableChar()).filter(_.isLetterOrDigit).take(8).mkString
  }

  private lazy val addUsersQuery = users returning users.map(_.id) into (
    (f, id) => f.copy(id = Some(id))
  )
}
