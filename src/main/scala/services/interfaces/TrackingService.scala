package org.aranadedoros.pricestream
package services.interfaces

import domain.dto.TrackPriceResponse
import domain.dto.TrackPriceRequest
import domain.errors.TrackingError
import domain.models.{PriceUpdate, TrackedProduct}

trait TrackingService[F[_]]:
  def trackPrice(
    request: TrackPriceRequest,
  ): F[Either[TrackingError, TrackPriceResponse]]

  def getHistory(
    platform: String,
    externalId: String
  ): F[Either[TrackingError, List[PriceUpdate]]]

  def listProducts(platform: Option[String]): F[List[TrackedProduct]]
