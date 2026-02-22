package org.aranadedoros.pricestream
package domain.dto

import io.circe.Codec
import java.time.Instant

// Use the 'derives' keyword for automatic derivation in Scala 3
final case class TrackPriceRequest(
                                    platform: String,
                                    externalId: String,
                                    price: BigDecimal,
                                    name: Option[String],
                                    url: Option[String]
                                  ) derives Codec.AsObject

final case class PriceUpdateResponse(
                                      price: BigDecimal,
                                      recordedAt: Instant
                                    ) derives Codec.AsObject

case class ProductResponse(
                            platform: String,
                            externalId: String,
                            name: Option[String],
                            url: Option[String]
                          ) derives Codec.AsObject