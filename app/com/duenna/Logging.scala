package com.cask

import org.slf4j.{Logger, LoggerFactory}

trait Logging {
  protected lazy val log: Logger = LoggerFactory.getLogger(this.getClass)

  def truncate(msg: String, length: Int = 400): String = {
    if (msg.length <= length) {
      msg
    } else {
      msg.take(length - 3) + "..."
    }
  }
}