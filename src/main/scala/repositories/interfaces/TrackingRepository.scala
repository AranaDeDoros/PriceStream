package org.aranadedoros.pricestream
package repositories.interfaces

import domain.models.*

import org.aranadedoros.pricestream.domain.dto.TrackPriceResponse

trait TrackingRepository[F[_]]:

  // Platform
  def findPlatformByName(name: String): F[Option[Platform]]
  def createPlatform(name: String): F[Platform]

  // Product
  def findProduct(platformId: Long, externalId: String): F[Option[CatalogueProduct]]
  def createProduct(
    platformId: Long,
    externalId: String,
    name: Option[String],
    url: Option[String]
  ): F[CatalogueProduct]

  // Price
  def insertPrice(productId: Long, price: BigDecimal): F[Unit]

  def getPriceHistory(productId: Long): F[List[PriceUpdate]]

  def listProducts: F[List[CatalogueProduct]]

  def listProductsByPlatform(platform: String): F[List[CatalogueProduct]]

  def insertTrackingRequest(platform: Platform, product: CatalogueProduct) : F[TrackPriceResponse]
  
  def getTrackingPriceHistory(url: String) : F[List[TrackedPriceRecord]]