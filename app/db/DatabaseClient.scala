package db

import play.api.db.slick.DatabaseConfigProvider
import com.github.tminglei.slickpg._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

trait DatabaseClient {
  //Users
  def addOrUpdateUser(userRow: UserRow): Future[Option[UserRow]]
  def listUsers(): Future[Seq[UserRow]]
}

class PostgresDatabaseClient @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[ExPostgresProfile]
  import dbConfig._
  import profile.api._
  private val users = TableQuery[UserTable]
  private lazy val addUserQuery = users returning users.map(_.id) into (
    (f, id) => f.copy(id = Some(id))
    )

  def addOrUpdateUser(userRow: UserRow): Future[UserRow] = db.run {
    for {
      maybeExisting: Option[UserRow] <- this.users.filter(_.id === userRow.id).result.headOption
      newRow: UserRow = maybeExisting match {
        case Some(r) => userRow.withId(r.id)
        case _ => userRow
      }
      result: Option[UserRow] <- addUserQuery.insertOrUpdate(newRow)
    } yield result.getOrElse(newRow)
  }

  def listUsers(): Future[Seq[UserRow]] = db.run {
    users.result
  }
}