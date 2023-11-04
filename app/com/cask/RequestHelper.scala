package com.cask

import play.api.libs.json.{JsError, JsSuccess, Json, Reads, Writes}
import play.api.mvc.{AnyContent, Request}

object RequestHelper {

  def requestToObjOrThrow[T]()(implicit request: Request[AnyContent], reads: Reads[T]):T = {
    request.body.asJson match {
      case Some(json) => {
        Json.fromJson[T](json)(reads) match {
          case JsSuccess(value, _) => value
          case JsError(errors) => throw new IllegalStateException("parsing error" + errors.toString())
          case _ => {
            throw new IllegalStateException("Unknown parsing error")
          }
        }
      }
      case _ => throw new IllegalStateException("Missing Body")
    }
  }

}


