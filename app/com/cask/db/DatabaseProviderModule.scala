package com.cask.db


import com.google.inject.Provides
import com.google.inject.Singleton
import com.typesafe.config.Config
import play.api.{Configuration, Environment}
import play.api.inject.{ApplicationLifecycle, Binding}
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import com.google.inject.AbstractModule
import com.google.inject.name.Names
import scala.concurrent.Future

class DatabaseProviderModule extends AbstractModule {
  @Provides
  @Singleton
  def getDatabaseConfig(config: Config, applicationLifecycle: ApplicationLifecycle): DatabaseConfig[JdbcProfile] = {
    val dbConfig = DatabaseConfig.forConfig[JdbcProfile]("slick.dbs.default", config)
    applicationLifecycle.addStopHook { () =>
      Future.successful(dbConfig.db.close())
    }
    dbConfig
  }

  @Provides
  @Singleton
  def getDatabaseProfile(dbConfig: DatabaseConfig[JdbcProfile]): JdbcProfile = {
    dbConfig.profile
  }

}
