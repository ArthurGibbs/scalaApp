package db

import akka.actor.ActorSystem
import com.google.inject.Inject
import com.google.inject.Singleton

import play.libs.concurrent.CustomExecutionContext

@Singleton
class DatabaseExecutionContext @Inject()(system: ActorSystem)
  extends CustomExecutionContext(system, "database-dispatcher")
