package com.cask

import com.google.inject.Inject
import io.jsonwebtoken.security.Keys

import java.time.Instant
import java.util.{Date, UUID}
import io.jsonwebtoken.{Claims, Jws, Jwts, SignatureAlgorithm}

import scala.jdk.CollectionConverters._
object Jwt {
  def apply(claims: Map[String, Any], secret: String, ttl: Int): String = {
    val key = Keys.hmacShaKeyFor(secret.getBytes("UTF-8"))
    val jwt = Jwts.builder()
      .setId(UUID.randomUUID.toString)
      .setIssuedAt(Date.from(Instant.now()))
      .setExpiration(Date.from(Instant.now().plusSeconds(ttl)))
      .signWith(key, SignatureAlgorithm.HS512)

    claims.foreach { case (name, value) =>
      jwt.claim(name, value)
    }

    jwt.compact()
  }

  def unapply(jwt: String, secret: String): Option[Map[String, Any]] = {
    try {
      val claims: Jws[Claims] = Jwts.parserBuilder()
        .setSigningKey(secret.getBytes("UTF-8")).build().parseClaimsJws(jwt)
      Option(claims.getBody.asScala.toMap)
    } catch {
      case _: Exception => None
    }
  }
}


