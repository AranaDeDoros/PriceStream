package org.aranadedoros.pricestream
package services

import cats.effect.Sync
import cats.syntax.all.*
import org.aranadedoros.pricestream.domain.errors.TrackingError
import org.aranadedoros.pricestream.domain.models.{Platform, PriceUpdate, TrackedProduct}
import org.aranadedoros.pricestream.repositories.interfaces.TrackingRepository
import org.aranadedoros.pricestream.services.interfaces.TrackingService

class LiveTrackingService[F[_]: Sync](
                                       repo: TrackingRepository[F]
                                     ) extends TrackingService[F] {

  private def getOrCreatePlatform(name: String): F[Platform] =
    repo.findPlatformByName(name).flatMap {
      case Some(p) => p.pure[F]
      case None    => repo.createPlatform(name)
    }

  private def getOrCreateProduct(
                                  platformId: Long,
                                  externalId: String,
                                  name: Option[String],
                                  url: Option[String]
                                ): F[TrackedProduct] =
    repo.findProduct(platformId, externalId).flatMap {
      case Some(p) => p.pure[F]
      case None    => repo.createProduct(platformId, externalId, name, url)
    }

  override def trackPrice(
                           platform: String,
                           externalId: String,
                           price: BigDecimal,
                           name: Option[String],
                           url: Option[String]
                         ): F[Either[TrackingError, Unit]] =

    if (price <= 0)
      TrackingError.InvalidPrice(price).asLeft.pure[F]
    else {

      val program =
        for {
          pl <- getOrCreatePlatform(platform)
          pr <- getOrCreateProduct(pl.id, externalId, name, url)
          _ <- repo.insertPrice(pr.id, price)
        } yield ()

      program
        .map(_.asRight[TrackingError])
        .handleError(e =>
          TrackingError.PersistenceError(e.getMessage).asLeft
        )
    }


  override def getHistory(
                           platform: String,
                           externalId: String
                         ): F[Either[TrackingError, List[PriceUpdate]]] =

    repo.findPlatformByName(platform).flatMap {
      case None =>
        TrackingError.ProductNotFound(platform, externalId)
          .asLeft[List[PriceUpdate]]
          .pure[F]

      case Some(pl) =>
        repo.findProduct(pl.id, externalId).flatMap {
          case None =>
            TrackingError.ProductNotFound(platform, externalId)
              .asLeft[List[PriceUpdate]]
              .pure[F]

          case Some(pr) =>
            repo.getPriceHistory(pr.id)
              .map(_.asRight[TrackingError])
        }
    }

  def listProducts(platform: Option[String]): F[List[TrackedProduct]] =
    platform match
      case Some(p) => repo.listProductsByPlatform(p)
      case None => repo.listProducts  


}
