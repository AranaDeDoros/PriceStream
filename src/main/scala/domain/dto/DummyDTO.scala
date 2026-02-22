package org.aranadedoros.pricestream
package domain.dto

import io.circe.Decoder
import io.circe.generic.semiauto.*

case class DummyProductDto(
  id: Long,
  title: String,
  price: Double
)

case class DummyProductsResponse(
  products: List[DummyProductDto]
)

object DummyProductDto:
  given Decoder[DummyProductDto] = deriveDecoder

object DummyProductsResponse:
  given Decoder[DummyProductsResponse] = deriveDecoder
