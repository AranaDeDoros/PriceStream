package org.aranadedoros.pricestream
package domain.errors

sealed trait TrackingError extends Product with Serializable

object TrackingError:

  final case class ProductNotFound(
    externalId: String
  ) extends TrackingError

  final case class PlatformNotFound(
    platform: String,
  ) extends TrackingError

  final case class InvalidPrice(
    value: BigDecimal
  ) extends TrackingError

  final case class PersistenceError(
    message: String
  ) extends TrackingError
