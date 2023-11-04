package com.cask

import com.google.inject.Inject
import pdi.jwt.{JwtAlgorithm, JwtJson}
import play.api.Configuration
import play.api.libs.json.{JsObject, JsValue}

import scala.util.{Failure, Success}


class Jwt @Inject() (config: Configuration){
  val secret = config.get[String]( "jwt.secret")
  val algo = JwtAlgorithm.HS256

  def encode(claim: JsObject): String = {
    JwtJson.encode(claim)
    val token = JwtJson.encode(claim, secret, algo)
    token
  }

  def decode(jws: String): Option[JsObject] = {
      JwtJson.decodeJson(jws, secret, Seq(JwtAlgorithm.HS256)) match {
        case Success(json) => {
          Some(json)
        }
        case Failure(exception) => None
      }
  }
}


