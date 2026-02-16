package org.aranadedoros.pricestream
package domain.models

import java.time.Instant

case class TrackedProduct(
                  id: Long,
                  platformId: Long,
                  externalId: String,
                  name: Option[String],
                  url: Option[String]
                  )

case class PriceUpdate(
                      price: BigDecimal,
                      recordedAt: Instant
                      )
