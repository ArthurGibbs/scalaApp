package com.cask.services

import com.google.common.hash.Hashing
import com.google.inject.Inject
import play.api.Configuration

import java.nio.charset.StandardCharsets

class AuthService @Inject() (config: Configuration) {
  lazy val serverSalt: String = config.get[String]( "app.salt")

  def getHashedPassword(rawPassword: String, salt: String): String = {
    Hashing.sha256().hashString(rawPassword+salt+serverSalt, StandardCharsets.UTF_8).toString
  }

}

