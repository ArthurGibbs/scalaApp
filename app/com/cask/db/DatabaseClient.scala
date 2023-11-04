package com.cask.db

import com.cask.db.dso.UserDSO
import com.google.inject.name.Named
import com.google.inject.{ImplementedBy, Inject}
import org.joda.time.DateTime
import play.api.db.Database

import java.sql.Types
import java.sql.{ResultSet, Timestamp}
import scala.collection.mutable.ListBuffer
import scala.concurrent.Future

@ImplementedBy(classOf[PostgresqlDatabaseClient])
trait DatabaseClient {
  //registration
  def isEmailUnused(email: String): Future[Boolean]


  def isUsernameUnused(username: String): Future[Boolean]
  //Users
  def addUser(userRow: UserDSO): Future[Option[UserDSO]]

  def updateUser(userRow: UserDSO): Future[Option[UserDSO]]
  def listUsers(): Future[Seq[UserDSO]]

  def getUserByName(username: String): Future[Option[UserDSO]]
  def getUserById(id: Int): Future[Option[UserDSO]]
  def getUserByEmail(email: String): Future[Option[UserDSO]]
}

@Inject @Named("MockDatabaseClient")
final class MockDatabaseClient @Inject()(db: Database, databaseExecutionContext: DatabaseExecutionContext) extends DatabaseClient{
  override def addUser(userRow: UserDSO): Future[Option[UserDSO]] = Future.successful(None)
  override def updateUser(userRow: UserDSO): Future[Option[UserDSO]] = Future.successful(None)

  override def listUsers(): Future[Seq[UserDSO]] = Future.successful(Seq())
  override def getUserByName(username: String): Future[Option[UserDSO]] = Future.successful(None)
  override def getUserById(id: Int): Future[Option[UserDSO]] = Future.successful(None)
  override def getUserByEmail(email: String): Future[Option[UserDSO]] = Future.successful(None)

  override def isEmailUnused(email: String): Future[Boolean] = Future.successful(true)
  override def isUsernameUnused(username: String): Future[Boolean] = Future.successful(true)
}

@Inject @Named("PostgresqlDatabaseClient")
final class PostgresqlDatabaseClient @Inject()(db: Database, databaseExecutionContext: DatabaseExecutionContext) extends DatabaseClient{

  override def addUser(userDSO: UserDSO): Future[Option[UserDSO]] = {
    Future {
      db.withConnection( conn => {
        //val stm = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)
        val stm = conn.prepareStatement("INSERT into Users.Users(" +
          "username, email, email_verified, email_verification_code, " +
          "hash, salt, profile_image_id, created_on, last_seen, gender, bio, bio_updated) " +
          "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING *;",
          ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)
        stm.setString(1, userDSO.username)
        stm.setString(2, userDSO.email)
        stm.setBoolean(3, userDSO.emailVerified)
        stm.setString(4, userDSO.emailVerificationCode)
        stm.setString(5, userDSO.hash)
        stm.setString(6, userDSO.salt)
        if (userDSO.profileImageId.isDefined) {
          stm.setInt(7, userDSO.profileImageId.get)
        } else {
          stm.setNull(7, java.sql.Types.INTEGER)
        }
        stm.setTimestamp(8, new Timestamp(userDSO.createdOn.getMillis()))
        stm.setTimestamp(9, new Timestamp(userDSO.lastSeen.getMillis()))

        if (userDSO.gender.isDefined) {
          stm.setString(10, userDSO.gender.get)
        } else {
          stm.setNull(10, java.sql.Types.VARCHAR)
        }

        stm.setString(11, userDSO.bio)

        if (userDSO.bioUpdated.isDefined) {
          stm.setTimestamp(12, new Timestamp(userDSO.bioUpdated.get.getMillis()))
        } else {
          stm.setNull(12, java.sql.Types.TIMESTAMP)
        }

        val rs = stm.executeQuery

        if (rs.next) {
          Some(UserDSO(
            Some(rs.getInt("id")),
            rs.getString("username"),
            rs.getString("email"),
            rs.getBoolean("email_verified"),
            rs.getString("email_verification_code"),
            rs.getString("hash"),
            rs.getString("salt"),
            Some(rs.getInt("profile_image_id")),
            new DateTime(rs.getTimestamp("created_on")),
            new DateTime(rs.getTimestamp("last_seen")),
            Some(rs.getString("gender")),
            rs.getString("bio"),
            Some(new DateTime(rs.getTimestamp("bio_updated")))
          ))
        } else {
          None
        }
      })
    }(databaseExecutionContext)
  }

  override def listUsers(): Future[Seq[UserDSO]] = {
    Future {
      db.withConnection( conn => {
        //val stm = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)
        val stm = conn.prepareStatement("Select * From Users.Users;",
          ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)
        val rs = stm.executeQuery

        val list: ListBuffer[UserDSO] = ListBuffer()
        while (rs.next) {
          list += UserDSO(
            Some(rs.getInt("id")),
            rs.getString("username"),
            rs.getString("email"),
            rs.getBoolean("email_verified"),
            rs.getString("email_verification_code"),
            rs.getString("hash"),
            rs.getString("salt"),
            Some(rs.getInt("profile_image_id")),
            new DateTime(rs.getTimestamp("created_on")),
            new DateTime(rs.getTimestamp("last_seen")),
            Some(rs.getString("gender")),
            rs.getString("bio"),
            Some(new DateTime(rs.getTimestamp("bio_updated")))
          )
        }
        list.toList
      })
    }(databaseExecutionContext)
  }

  override def getUserByName(username: String): Future[Option[UserDSO]] = {
    Future {
      db.withConnection( conn => {
        val stm = conn.prepareStatement("Select * From Users.Users WHERE Users.username = ?",
          ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)
        stm.setString(1, username)
        val rs = stm.executeQuery

        val list: ListBuffer[UserDSO] = ListBuffer()
        if (rs.next) {
          Some(UserDSO(
            Some(rs.getInt("id")),
            rs.getString("username"),
            rs.getString("email"),
            rs.getBoolean("email_verified"),
            rs.getString("email_verification_code"),
            rs.getString("hash"),
            rs.getString("salt"),
            Some(rs.getInt("profile_image_id")),
            new DateTime(rs.getTimestamp("created_on")),
            new DateTime(rs.getTimestamp("last_seen")),
            Some(rs.getString("gender")),
            rs.getString("bio"),
            Some(new DateTime(rs.getTimestamp("bio_updated")))
          ))
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

  override def getUserById(id: Int): Future[Option[UserDSO]] = {
    Future {
      db.withConnection( conn => {
        val stm = conn.prepareStatement("Select * From Users.Users WHERE Users.id = ?",
          ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)
        stm.setInt(1, id)
        val rs = stm.executeQuery

        if (rs.next) {
          Some(UserDSO(
            Some(rs.getInt("id")),
            rs.getString("username"),
            rs.getString("email"),
            rs.getBoolean("email_verified"),
            rs.getString("email_verification_code"),
            rs.getString("hash"),
            rs.getString("salt"),
            Some(rs.getInt("profile_image_id")),
            new DateTime(rs.getTimestamp("created_on")),
            new DateTime(rs.getTimestamp("last_seen")),
            Some(rs.getString("gender")),
            rs.getString("bio"),
            Some(new DateTime(rs.getTimestamp("bio_updated")))
          ))
        } else {
          None
        }
      })
    }(databaseExecutionContext)
  }

  override def updateUser(userDSO: UserDSO): Future[Option[UserDSO]] = {
    Future {
      db.withConnection( conn => {
        //val stm = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)
        val stm = conn.prepareStatement("UPDATE Users.Users SET " +
          "username = ?, email= ?,  email_verified= ?,  email_verification_code = ?, " +
          "hash = ?,  salt = ?, profile_image_id = ?, created_on = ?, last_seen = ?, gender = ?, bio = ?, bio_updated = ? " +
          "WHERE Users.id = ? RETURNING *;",
          ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)
        stm.setString(1, userDSO.username)
        stm.setString(2, userDSO.email)
        stm.setBoolean(3, userDSO.emailVerified)
        stm.setString(4, userDSO.emailVerificationCode)
        stm.setString(5, userDSO.hash)
        stm.setString(6, userDSO.salt)
        if (userDSO.profileImageId.isDefined) {
          stm.setInt(7, userDSO.profileImageId.get)
        } else {
          stm.setNull(7, java.sql.Types.INTEGER)
        }
        stm.setTimestamp(8, new Timestamp(userDSO.createdOn.getMillis()))
        stm.setTimestamp(9, new Timestamp(userDSO.lastSeen.getMillis()))

        if (userDSO.gender.isDefined) {
          stm.setString(10, userDSO.gender.get)
        } else {
          stm.setNull(10, java.sql.Types.VARCHAR)
        }

        stm.setString(11, userDSO.bio)

        if (userDSO.bioUpdated.isDefined) {
          stm.setTimestamp(12, new Timestamp(userDSO.bioUpdated.get.getMillis()))
        } else {
          stm.setNull(12, java.sql.Types.TIMESTAMP)
        }
        stm.setInt(13, userDSO.id.getOrElse(0))


        val rs = stm.executeQuery

        if (rs.next) {
          Some(UserDSO(
            Some(rs.getInt("id")),
            rs.getString("username"),
            rs.getString("email"),
            rs.getBoolean("email_verified"),
            rs.getString("email_verification_code"),
            rs.getString("hash"),
            rs.getString("salt"),
            Some(rs.getInt("profile_image_id")),
            new DateTime(rs.getTimestamp("created_on")),
            new DateTime(rs.getTimestamp("last_seen")),
            Some(rs.getString("gender")),
            rs.getString("bio"),
            Some(new DateTime(rs.getTimestamp("bio_updated")))
          ))
        } else {
          None
        }
      })
    }(databaseExecutionContext)
  }

  override def getUserByEmail(email: String): Future[Option[UserDSO]] = {
    Future {
      db.withConnection( conn => {
        val stm = conn.prepareStatement("Select * From Users.Users WHERE Users.email = ?",
          ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)
        stm.setString(1, email)
        val rs = stm.executeQuery

        if (rs.next) {
          Some(UserDSO(
            Some(rs.getInt("id")),
            rs.getString("username"),
            rs.getString("email"),
            rs.getBoolean("email_verified"),
            rs.getString("email_verification_code"),
            rs.getString("hash"),
            rs.getString("salt"),
            Some(rs.getInt("profile_image_id")),
            new DateTime(rs.getTimestamp("created_on")),
            new DateTime(rs.getTimestamp("last_seen")),
            Some(rs.getString("gender")),
            rs.getString("bio"),
            Some(new DateTime(rs.getTimestamp("bio_updated")))
          ))
        } else {
          None
        }
      })
    }(databaseExecutionContext)
  }
}