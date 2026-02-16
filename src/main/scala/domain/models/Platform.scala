package org.aranadedoros.pricestream
package domain.models

import io.circe.generic.semiauto.*
import io.circe.{Decoder, Encoder}

final case class Platform(
                           id: Long,
                           name: String,
                           baseUrl: String
                         )

object Platform:
  given Encoder[Platform] = deriveEncoder
  given Decoder[Platform] = deriveDecoder

sealed trait PlatformProvider

object PlatformProvider :
  case object DummyJson extends PlatformProvider
//  case object Amazon extends PlatformProvider
//  case object MercadoLibre extends PlatformProvider

  def fromString(value: String): Either[String, PlatformProvider] =
    value.toLowerCase match
      case "dummyjson"     => Right(DummyJson)
//      case "amazon"        => Right(Amazon)
//      case "mercadolibre"  => Right(MercadoLibre)
      case other           => Left(s"Unknown provider: $other")

