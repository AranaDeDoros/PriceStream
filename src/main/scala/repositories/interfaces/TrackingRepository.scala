package org.aranadedoros.pricestream
package repositories.interfaces

import domain.models.*


trait TrackingRepository[F[_]] {

  // Platform
  def findPlatformByName(name: String): F[Option[Platform]]
  def createPlatform(name: String): F[Platform]

  // Product
  def findProduct(platformId: Long, externalId: String): F[Option[TrackedProduct]]
  def createProduct(
                     platformId: Long,
                     externalId: String,
                     name: Option[String],
                     url: Option[String]
                   ): F[TrackedProduct]

  // Price
  def insertPrice(productId: Long, price: BigDecimal): F[Unit]

  def getPriceHistory(productId: Long): F[List[PriceUpdate]]

  def listProducts: F[List[TrackedProduct]]

  def listProductsByPlatform(platform: String): F[List[TrackedProduct]]

}
