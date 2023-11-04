package com.cask.models

import org.joda.time.{DateTime, DateTimeZone}
import org.joda.time.format.{DateTimeFormat, ISODateTimeFormat}
import play.api.libs.json.{Format, JsResult, JsString, JsSuccess, JsValue, OFormat, Reads, Writes}

class JodaDateTimeReadsWrites {}

object JodaDateTimeReadsWrites {
  private lazy val ISODateTimeFormatter = ISODateTimeFormat.dateTime.withZone(DateTimeZone.UTC)
  private lazy val ISODateTimeParser = ISODateTimeFormat.dateTimeParser

  implicit val dateTimeFormatter: Format[DateTime] = new Format[DateTime] {
    def reads(j: JsValue): JsSuccess[DateTime] = JsSuccess(ISODateTimeParser.parseDateTime(j.as[String]))
    def writes(o: DateTime): JsValue = JsString(ISODateTimeFormatter.print(o))
  }
}
