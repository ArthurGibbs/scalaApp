package db

import com.google.inject.name.Named
import com.google.inject.{ImplementedBy, Inject, Provides}
import play.api.db.Database

import java.sql.ResultSet
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

  def updateSomething(): Unit = {
    Future {
      // get jdbc connection
      val connection = db.withConnection( conn => {
        System.out.println("cheese")
        val stm = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)
        val rs = stm.executeQuery("SELECT * from Users.Users;")
        while (rs.next) {
          System.out.println(rs.getString("username"))
        }
      }
      )
    }(databaseExecutionContext)

  }

  override def addOrUpdateUser(userRow: UserRow): Future[Option[UserRow]] = {

    updateSomething()
    Future(None)(databaseExecutionContext)
  }

  override def listUsers(): Future[Seq[UserRow]] = {
    updateSomething()
    Future(Seq())(databaseExecutionContext)
    //is this the right ec?
  }
}