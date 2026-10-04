package org.aranadedoros.pricestream
package domain.models

import java.time.Instant

case class CatalogueProduct(
  id: Long,
  platformId: Long,
  externalId: String,
  name: Option[String],
  url: Option[String],
  price: BigDecimal
)

case class PriceUpdate(
  price: BigDecimal,
  recordedAt: Instant
)
