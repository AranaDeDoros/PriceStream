package org.aranadedoros.pricestream
package domain.models

import java.net.URLEncoder
import java.nio.charset.StandardCharsets


enum TrackingStatuses:
    case Tracking, Stopped
    
case class TrackingParams(
  platform: String,
  externalId: String,
)

def trackingUrl(base: String, params: TrackingParams): String =
  val values = List(
    "plat"   -> params.platform,
    "exid"   -> params.externalId,
  ) 

  val query = values
    .map { case (key, value) =>
      s"$key=${URLEncoder.encode(value, StandardCharsets.UTF_8)}"
    }
    .mkString("&")

  s"$base?$query"