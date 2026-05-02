package org.aranadedoros.pricestream
package services

import domain.models.TrackedProduct
import repositories.interfaces.{PriceRepository, ProductRepository}
import services.providers.ProductProvider

import cats.effect.IO
import cats.syntax.all.*

class IngestService(
  providers: Seq[ProductProvider],
  productRepo: ProductRepository,
  priceRepo: PriceRepository
):

  def ingestOnce(): IO[Int] =
    providers.parTraverse(ingestFromProvider).map(_.sum)

  private def ingestFromProvider(provider: ProductProvider): IO[Int] =
    for {
      products <- provider.fetchProducts()
      count    <- products.traverse(processProduct(provider, _)).map(_.size)
    } yield count

  private def processProduct(
    provider: ProductProvider,
    externalProduct: TrackedProduct
  ): IO[Unit] =
    for {
      persisted <- ensureProductExists(externalProduct)
      _         <- updatePriceIfChanged(provider, persisted)
    } yield ()

  private def ensureProductExists(
    p: TrackedProduct
  ): IO[TrackedProduct] =
    productRepo
      .findByExternalId(p.platformId, p.externalId)
      .flatMap {
        case Some(existing) => IO.pure(existing)
        case None           => productRepo.insert(p)
      }

  private def updatePriceIfChanged(
    provider: ProductProvider,
    product: TrackedProduct
  ): IO[Unit] =
    for {
      maybeNewPrice <- provider.fetchPrice(product.externalId)
      _ <- maybeNewPrice match {
        case None => IO.unit
        case Some(newPrice) =>
          priceRepo.findLatestPrice(product.id).flatMap {
            case Some(lastPrice) if lastPrice == newPrice =>
              IO.unit
            case _ =>
              priceRepo.insert(product.id, newPrice)
          }
      }
    } yield ()
