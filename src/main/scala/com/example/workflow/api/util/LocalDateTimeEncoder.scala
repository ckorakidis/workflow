package com.example.workflow.api.util

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import io.circe.Decoder.Result
import io.circe.{ Decoder, Encoder, HCursor, Json }

class LocalDateTimeEncoder extends Encoder[LocalDateTime] with Decoder[LocalDateTime] {
  val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

  override def apply(a: LocalDateTime): Json =
    Encoder.encodeString.apply(a.format(formatter))

  override def apply(c: HCursor): Result[LocalDateTime] =
    Decoder.decodeString.map(s => LocalDateTime.parse(s, formatter)).apply(c)
}

