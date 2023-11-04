package com.cask.db

import com.cask.models.user.{PersonalUser, PublicUser, ServerUser}
import com.google.inject.name.Named
import com.google.inject.{ImplementedBy, Inject}
import org.joda.time.DateTime
import play.api.db.Database

import java.sql.{PreparedStatement, ResultSet, Timestamp, Types}
import scala.collection.mutable.ListBuffer
import scala.concurrent.Future

@ImplementedBy(classOf[PostgresqlDatabaseClient])
trait DatabaseClient {
  //registration
  def isEmailUnused(email: String): Future[Boolean]
  def isUsernameUnused(username: String): Future[Boolean]
  //Users
  def addUser(userRow: ServerUser): Future[Option[ServerUser]]

  def updateUser(userRow: ServerUser): Future[Option[ServerUser]]
  def listUsers(): Future[Seq[ServerUser]]

  def getUserByName(username: String): Future[Option[ServerUser]]
  def getUserById(id: Int): Future[Option[ServerUser]]
  def getUserByEmail(email: String): Future[Option[ServerUser]]
}

@Inject @Named("MockDatabaseClient")
final class MockDatabaseClient @Inject()(db: Database, databaseExecutionContext: DatabaseExecutionContext) extends DatabaseClient{
  override def addUser(userRow: ServerUser): Future[Option[ServerUser]] = Future.successful(None)
  override def updateUser(userRow: ServerUser): Future[Option[ServerUser]] = Future.successful(None)

  override def listUsers(): Future[Seq[ServerUser]] = Future.successful(Seq())
  override def getUserByName(username: String): Future[Option[ServerUser]] = Future.successful(None)
  override def getUserById(id: Int): Future[Option[ServerUser]] = Future.successful(None)
  override def getUserByEmail(email: String): Future[Option[ServerUser]] = Future.successful(None)

  override def isEmailUnused(email: String): Future[Boolean] = Future.successful(true)
  override def isUsernameUnused(username: String): Future[Boolean] = Future.successful(true)
}

@Inject @Named("PostgresqlDatabaseClient")
final class PostgresqlDatabaseClient @Inject()(db: Database, databaseExecutionContext: DatabaseExecutionContext) extends DatabaseClient{

  override def addUser(serverUser: ServerUser): Future[Option[ServerUser]] = {
    Future {
      db.withConnection( conn => {
        val stm = conn.prepareStatement("INSERT into Users.Users(" +
          "username, email, email_verified, email_verification_code, " +
          "hash, salt, profile_image_id, created_on, last_seen, gender, bio, bio_updated) " +
          "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING *;",
          ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)
        stm.setString(1, serverUser.user.displayUser.username)
        stm.setString(2, serverUser.user.email)
        stm.setBoolean(3, serverUser.user.emailVerified)
        stm.setString(4, serverUser.emailVerificationCode)
        stm.setString(5, serverUser.hash)
        stm.setString(6, serverUser.salt)
        setOptionalInt(stm,7,serverUser.user.displayUser.profileImageId)
        stm.setTimestamp(8, new Timestamp(serverUser.user.displayUser.createdOn.getMillis()))
        stm.setTimestamp(9, new Timestamp(serverUser.user.displayUser.lastSeen.getMillis()))
        setOptionalString(stm,10,serverUser.user.displayUser.gender)
        stm.setString(11, serverUser.user.displayUser.bio)
        setOptionalDateTime(stm,12,serverUser.user.displayUser.bioUpdated)

        val rs = stm.executeQuery

        if (rs.next) {
          val du = PublicUser.fromResultSet(rs)
          val user = PersonalUser.fromResultSet(rs,du)
          val su = ServerUser.fromResultSet(rs,user)
          Some(su)
        } else {
          None
        }

      })
    }(databaseExecutionContext)
  }

  override def listUsers(): Future[Seq[ServerUser]] = {
    Future {
      db.withConnection( conn => {
        val stm = conn.prepareStatement("Select * From Users.Users;",
          ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)
        val rs = stm.executeQuery

        val list: ListBuffer[ServerUser] = ListBuffer()
        while (rs.next) {
          val du = PublicUser.fromResultSet(rs)
          val user = PersonalUser.fromResultSet(rs,du)
          val su = ServerUser.fromResultSet(rs,user)
          list += su
        }
        list.toList
      })
    }(databaseExecutionContext)
  }

  override def getUserByName(username: String): Future[Option[ServerUser]] = {
    Future {
      db.withConnection( conn => {
        val stm = conn.prepareStatement("Select * From Users.Users WHERE Users.username = ?",
          ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)
        stm.setString(1, username)
        val rs = stm.executeQuery

        if (rs.next) {
          val du = PublicUser.fromResultSet(rs)
          val user = PersonalUser.fromResultSet(rs,du)
          val su = ServerUser.fromResultSet(rs,user)
          Some(su)
        } else {
          None
        }
      })
    }(databaseExecutionContext)
  }

  override def isEmailUnused(email: String): Future[Boolean] = {
    Future {
      db.withConnection( conn => {
        val stm = conn.prepareStatement("Select email From Users.Users WHERE Users.email = ?",
          ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)
        stm.setString(1, email)
        val rs = stm.executeQuery
        if (rs.next) {
          false
        } else {
          true
        }
      })
    }(databaseExecutionContext)
  }

  override def isUsernameUnused(username: String): Future[Boolean] = {
    Future {
      db.withConnection( conn => {
        val stm = conn.prepareStatement("Select username From Users.Users WHERE Users.username = ?",
          ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)
        stm.setString(1, username)
        val rs = stm.executeQuery
        if (rs.next) {
          false
        } else {
          true
        }
      })
    }(databaseExecutionContext)
  }

  override def getUserById(id: Int): Future[Option[ServerUser]] = {
    Future {
      db.withConnection( conn => {
        val stm = conn.prepareStatement("Select * From Users.Users WHERE Users.id = ?",
          ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)
        stm.setInt(1, id)
        val rs = stm.executeQuery

        if (rs.next) {
          val du = PublicUser.fromResultSet(rs)
          val user = PersonalUser.fromResultSet(rs,du)
          val su = ServerUser.fromResultSet(rs,user)
          Some(su)
        } else {
          None
        }
      })
    }(databaseExecutionContext)
  }

  override def updateUser(serverUser: ServerUser): Future[Option[ServerUser]] = {
    Future {
      db.withConnection( conn => {
        //val stm = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)
        val stm = conn.prepareStatement("UPDATE Users.Users SET " +
          "username = ?, email= ?,  email_verified= ?,  email_verification_code = ?, " +
          "hash = ?,  salt = ?, profile_image_id = ?, created_on = ?, last_seen = ?, gender = ?, bio = ?, bio_updated = ?, password_reset_code = ? " +
          "WHERE Users.id = ? RETURNING *;",
          ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)
        stm.setString(1, serverUser.user.displayUser.username)
        stm.setString(2, serverUser.user.email)
        stm.setBoolean(3, serverUser.user.emailVerified)
        stm.setString(4, serverUser.emailVerificationCode)
        stm.setString(5, serverUser.hash)
        stm.setString(6, serverUser.salt)
        setOptionalInt(stm,7,serverUser.user.displayUser.profileImageId)
        stm.setTimestamp(8, new Timestamp(serverUser.user.displayUser.createdOn.getMillis()))
        stm.setTimestamp(9, new Timestamp(serverUser.user.displayUser.lastSeen.getMillis()))
        setOptionalString(stm,10,serverUser.user.displayUser.gender)
        stm.setString(11, serverUser.user.displayUser.bio)
        setOptionalDateTime(stm,12,serverUser.user.displayUser.bioUpdated)
        setOptionalString(stm,13,serverUser.passwordResetCode)


        stm.setInt(14, serverUser.user.displayUser.id.getOrElse(0))//todo prevent user id 0


        val rs = stm.executeQuery

        if (rs.next) {
          val du = PublicUser.fromResultSet(rs)
          val user = PersonalUser.fromResultSet(rs,du)
          val su = ServerUser.fromResultSet(rs,user)
          Some(su)
        } else {
          None
        }
      })
    }(databaseExecutionContext)
  }

  override def getUserByEmail(email: String): Future[Option[ServerUser]] = {
    Future {
      db.withConnection( conn => {
        val stm = conn.prepareStatement("Select * From Users.Users WHERE Users.email = ?",
          ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)
        stm.setString(1, email)
        val rs = stm.executeQuery

        if (rs.next) {
          val du = PublicUser.fromResultSet(rs)
          val user = PersonalUser.fromResultSet(rs,du)
          val su = ServerUser.fromResultSet(rs,user)
          Some(su)
        } else {
          None
        }
      })
    }(databaseExecutionContext)
  }

  private def setOptionalInt(stm: PreparedStatement, position: Int, value: Option[Int]) = {
    value match {
      case Some(int) => stm.setInt(position, int)
      case _ => stm.setNull(position, java.sql.Types.INTEGER)
    }
  }

  private def setOptionalString(stm: PreparedStatement, position: Int, value: Option[String]) = {
    value match {
      case Some(string) => stm.setString(position, string)
      case _ => stm.setNull(position, java.sql.Types.VARCHAR)
    }
  }

  private def setOptionalDateTime(stm: PreparedStatement, position: Int, value: Option[DateTime]) = {
    value match {
      case Some(dateTime) =>    stm.setTimestamp(position, new Timestamp(dateTime.getMillis()))
      case _ => stm.setNull(position, java.sql.Types.TIMESTAMP)
    }
  }

}