package org.aranadedoros.pricestream
package domain.dto

import io.circe.Codec
import io.circe.generic.semiauto.*

import java.time.Instant

final case class TrackPriceRequest(
                                    platform: String,
                                    externalId: String,
                                    price: BigDecimal,
                                    name: Option[String],
                                    url: Option[String]
                                  )

final case class PriceUpdateResponse(
                                      price: BigDecimal,
                                      recordedAt: Instant
                                    )
case class ProductResponse(
                            platform: String,
                            externalId: String,
                            name: Option[String],
                            url: Option[String]
                          )


given Codec[TrackPriceRequest] = deriveCodec
given Codec[PriceUpdateResponse] = deriveCodec
given Codec[ProductResponse] = deriveCodec