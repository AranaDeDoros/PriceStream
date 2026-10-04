package org.aranadedoros.pricestream
package domain.models

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.Instant

object TrackedPriceRecord:
  given Encoder[TrackedPriceRecord] = deriveEncoder
  given Decoder[TrackedPriceRecord] = deriveDecoder

enum TrackingStatuses:
  case Tracking, Stopped

case class TrackingParams(
  platform: String,
  externalId: String
)

case class TrackedPriceRecord(
  name: String,
  price: BigDecimal,
  platform: Platform,
  recordedAt: Instant,
  trackedPrice: BigDecimal
)

object TrackingStatuses:

  def fromString(value: String): Either[String, TrackingStatuses] =
    value match
      case "Tracking" => Right(Tracking)
      case "Stopped"  => Right(Stopped)
      case other      => Left(s"Unknown tracking status: $other")

def trackingUrl(base: String, params: TrackingParams): String =
  val values = List(
    "plat" -> params.platform,
    "exid" -> params.externalId
  )

  val query = values
    .map {
      case (key, value) =>
        s"$key=${URLEncoder.encode(value, StandardCharsets.UTF_8)}"
    }
    .mkString("&")

  s"$base?$query"
