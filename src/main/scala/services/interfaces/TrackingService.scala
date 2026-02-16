package org.aranadedoros.pricestream
package services.interfaces

import domain.errors.TrackingError
import domain.models.{PriceUpdate, TrackedProduct}

trait TrackingService[F[_]] :
  def trackPrice(
                  platform: String,
                  externalId: String,
                  price: BigDecimal,
                  name: Option[String],
                  url: Option[String]
                ): F[Either[TrackingError, Unit]]

  def getHistory(
                  platform: String,
                  externalId: String
                ): F[Either[TrackingError, List[PriceUpdate]]]

  def listProducts(platform: Option[String]): F[List[TrackedProduct]]