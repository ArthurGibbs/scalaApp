package com.cask.db

import akka.actor.ActorSystem
import com.google.inject.Inject
import com.google.inject.Singleton

import play.libs.concurrent.CustomExecutionContext

/**
 * This class is a pointer to an execution context configured to point to "database.dispatcher"
 * in the "application.conf" file.
 */
@Singleton
class DatabaseExecutionContext @Inject()(system: ActorSystem) extends CustomExecutionContext(system, "database.dispatcher")
