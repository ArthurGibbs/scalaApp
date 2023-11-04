package db


import com.google.inject.name.Named
import com.google.inject.{ImplementedBy, Inject, Provides}
import play.api.db.Database
import scala.concurrent.Future

@ImplementedBy(classOf[PostgresqlDatabaseClient])
trait DatabaseClient {
  //Users
  def addOrUpdateUser(userRow: UserRow): Future[Option[UserRow]]
  def listUsers(): Future[Seq[UserRow]]
}

@Inject @Named("MockDatabaseClient")
final class MockDatabaseClient @Inject()(db: Database, databaseExecutionContext: DatabaseExecutionContext) extends DatabaseClient{
  override def addOrUpdateUser(userRow: UserRow): Future[Option[UserRow]] = Future.successful(None)
  override def listUsers(): Future[Seq[UserRow]] = Future.successful(Seq())
}

@Inject @Named("PostgresqlDatabaseClient")
final class PostgresqlDatabaseClient @Inject()(db: Database, databaseExecutionContext: DatabaseExecutionContext) extends DatabaseClient{

  override def addOrUpdateUser(userRow: UserRow): Future[Option[UserRow]] = {
    Future {
      // get jdbc connection
      val result = db.withConnection( conn => {
        //val stm = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)
        val stm = conn.prepareStatement("INSERT into Users.Users(username, email, hash) VALUES (?, ?, ?) RETURNING *;")
        stm.setString(1, userRow.name)
        stm.setString(2, userRow.email)
        stm.setString(3, userRow.hash)
        val rs = stm.executeQuery

        if (rs.next) {
          Some(UserRow(Some(rs.getInt("id")),rs.getString("username"),rs.getString("email"),rs.getString("hash")))
        } else {
          None
        }
      })
      result
    }(databaseExecutionContext)
  }

  override def listUsers(): Future[Seq[UserRow]] = {
    Future(Seq())(databaseExecutionContext)
    //is this the right ec?
  }
}