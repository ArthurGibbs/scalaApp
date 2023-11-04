package com.cask.models

import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.{JsPath, Reads, Writes}

case class Role(id: Int, roleName: String) {}

object Role {

  implicit val RoleWrites: Writes[Role] =
    (JsPath \ "id").write[Int]
      .and((JsPath \ "roleName").write[String])(unlift(Role.unapply))

  implicit val RoleReads: Reads[Role] =
    (JsPath \ "id").read[Int]
      .and((JsPath \ "roleName").read[String])(Role.apply _)

}






