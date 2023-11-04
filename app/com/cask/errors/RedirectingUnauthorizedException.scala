package com.cask.errors

import com.cask.errors.RedirectingUnauthorizedException.mkMessage


class RedirectingUnauthorizedException(message: String, redirect: String) extends RuntimeException(mkMessage(message, redirect)) {
  override def getMessage(): String = {message}
  def getRedirect(): String = {redirect}
}

object RedirectingUnauthorizedException {
  private def mkMessage(message: String, redirect: String): String = {
    s"Access Denied because [${message}] authenticate at [${redirect}]"
  }

 }

