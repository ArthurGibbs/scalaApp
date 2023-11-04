package com.cask.services

import com.google.inject.Inject

import java.util.Properties
import javax.mail._
import javax.mail.internet._
import org.slf4j.{Logger, LoggerFactory}
import play.api.Configuration
import play.api.libs.json.{JsValue, Json}
import scalaj.http.{Http, HttpOptions, HttpResponse}

import java.net.URLEncoder

class EmailUtil @Inject() (config: Configuration){

  final val EMPTY_STRING = ""
  val logger: Logger = LoggerFactory.getLogger(this.getClass())
  val senderEmail = try {
    config.get[String]( "mail.smtp.sender")
  } catch {
    case ex: Exception =>
      logger.error("Email key not set.\nPlease add email variable using -> export email=your_email@domain.com")
      EMPTY_STRING
  }

  val hostName = "smtp.gmail.com"
  val port = "587"

  val properties = new Properties
  properties.setProperty("mail.smtp.ssl.enable", "true"); // required for Gmail
  properties.setProperty("mail.smtp.auth.mechanisms", "XOAUTH2");

  val session = Session.getDefaultInstance(properties)
  val mimeSender = config.get[String]( "mail.smtp.mimeSender")
  val TOKEN_URL = "https://www.googleapis.com/oauth2/v4/token"
  val oauthClientId = config.get[String]( "mail.smtp.clientId")
  val oauthSecret = config.get[String]( "mail.smtp.clientSecret")
  val refreshToken = config.get[String]( "mail.smtp.refreshToken")
  var accessToken = ""
  var tokenExpires = 1458168133864L


  def refreshAccessToken() = {

    if (System.currentTimeMillis > tokenExpires) {
      val request = "client_id=" + URLEncoder.encode(oauthClientId, "UTF-8") + "&client_secret=" + URLEncoder.encode(oauthSecret, "UTF-8") + "&refresh_token=" + URLEncoder.encode(refreshToken, "UTF-8") + "&grant_type=refresh_token"

      val result = Http(TOKEN_URL + "?" + request).postData("")
        .header("Content-Type", "application/json")
        .header("Charset", "UTF-8")
        .header("Accept", "application/json; charset=utf-8")
        .header("Host", "localhost:9000")
        .option(HttpOptions.readTimeout(10000)).asString

      result match {
        case HttpResponse(body, 200, headers) => {
          val json: JsValue = Json.parse(body)
          try {
            accessToken = (json \ "access_token").toOption.get.toString()
            tokenExpires = (json \ "expires_in").toOption.get.toString().toLong * 1000 + System.currentTimeMillis
          } catch {
            case ex : Throwable => {
              throw new IllegalStateException("Error parsing access token json response")
            }
          }
        }
        case _ => {
          throw new IllegalStateException("Error refreshing token")
        }
      }
    }
  }

  def sendMail(recipient: List[String], subject: String, content: String): Option[Int] = {
    try {
      val message = new MimeMessage(session)
      message.setFrom(new InternetAddress(senderEmail))
      val recipientAddress: Array[Address] = (recipient map { recipient => new InternetAddress(recipient) }).toArray
      message.addRecipients(Message.RecipientType.TO, recipientAddress)
      message.setSubject(subject)
      message.setHeader("Content-Type", "text/plain;")
      message.setContent(content, "text/html")
      val transport = session.getTransport("smtp")
      transport.connect(hostName, senderEmail, accessToken)
      transport.sendMessage(message, message.getAllRecipients)
      logger.info("Email Sent!!")
      Some(recipient.size)
    }
    catch {
      case exception: Exception =>
        logger.error("Mail delivery failed. " + exception)
        None
    }
  }

  def sendMail(recipient: String, subject: String, content: String): Option[Int] = {
    refreshAccessToken()
    try {
      val message = new MimeMessage(session)
      message.setFrom(new InternetAddress(mimeSender))
      message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient))
      message.setSubject(subject)
      message.setHeader("Content-Type", "text/html")
      message.setContent(content, "text/html; charset=UTF-8")
      val transport = session.getTransport("smtp")
      transport.connect(hostName, senderEmail, accessToken)
      transport.sendMessage(message, message.getAllRecipients)
      logger.info("Email Sent!!")
      Some(recipient.length)
    }
    catch {
      case exception: Exception =>
        logger.error("Mail delivery failed. " + exception)
        None
    }
  }

}