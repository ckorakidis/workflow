package com.example.workflow.api.util

import io.circe.{ Encoder, Json }
import io.finch.{ Error, Errors }

object ErrorEncoder {

  private def errorToJson(e: Error): Json = e match {
    case Error.NotPresent(_) =>
      Json.obj("error" -> Json.fromString("something_not_present"))
    case Error.NotParsed(_, _, _) =>
      Json.obj("error" -> Json.fromString("something_not_parsed"))
    case Error.NotValid(_, _) =>
      Json.obj("error" -> Json.fromString("something_not_valid"))
  }

  def apply(): Encoder[Exception] =
    Encoder.instance {
      case e: Error => errorToJson(e)
      case Errors(nel) => Json.arr(nel.toList.map(errorToJson): _*)
      case ex: Exception => Json.obj("error" -> Json.fromString(ex.getMessage))
    }
}
