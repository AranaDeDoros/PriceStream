package org.aranadedoros.pricestream
package repositories

import domain.models
import domain.models.{Platform, PriceUpdate, TrackedProduct}
import repositories.interfaces.TrackingRepository
import cats.effect.Async
import cats.syntax.all.*
import doobie.Transactor
import doobie.implicits.*
import doobie.postgres.implicits.*


class DoobieTrackingRepository[F[_]: Async](
                                             xa: Transactor[F]
                                           ) extends TrackingRepository[F] {
  
  // Platform

  def findPlatformByName(name: String): F[Option[Platform]] =
    sql"""
      SELECT id, name
      FROM platforms
      WHERE name = $name
    """.query[Platform]
      .option
      .transact(xa)

  def createPlatform(name: String): F[Platform] =
    sql"""
      INSERT INTO platforms (name)
      VALUES ($name)
      RETURNING id, name
    """.query[Platform]
      .unique
      .transact(xa)

  // Product

  def findProduct(platformId: Long, externalId: String): F[Option[TrackedProduct]] =
    sql"""
      SELECT id, platform_id, external_id, name, url
      FROM products
      WHERE platform_id = $platformId
        AND external_id = $externalId
    """.query[TrackedProduct]
      .option
      .transact(xa)

  def createProduct(
                     platformId: Long,
                     externalId: String,
                     name: Option[String],
                     url: Option[String]
                   ): F[TrackedProduct] =
    sql"""
      INSERT INTO products (platform_id, external_id, name, url)
      VALUES ($platformId, $externalId, $name, $url)
      RETURNING id, platform_id, external_id, name, url
    """.query[TrackedProduct]
      .unique
      .transact(xa)

  // Price

  def insertPrice(productId: Long, price: BigDecimal): F[Unit] =
    sql"""
      INSERT INTO price_history (product_id, price)
      VALUES ($productId, $price)
    """.update
      .run
      .transact(xa)
      .void

  def getPriceHistory(productId: Long): F[List[PriceUpdate]] =
    sql"""
      SELECT price, recorded_at
      FROM price_history
      WHERE product_id = $productId
      ORDER BY recorded_at ASC
    """.query[PriceUpdate]
      .to[List]
      .transact(xa)

  override def listProducts: F[List[TrackedProduct]] =
    sql"""
      SELECT pr.id, pr.platform, pr.external_id, pr.name, pr.url
      FROM products pr
    """
      .query[TrackedProduct]
      .to[List]
      .transact(xa)

  override def listProductsByPlatform(platform: String): F[List[TrackedProduct]] =
    sql"""
      SELECT pr.id, pr.platform_id, pr.external_id, pr.name, pr.url
      FROM products pr
      JOIN platforms pl ON pl.id = pr.platform_id
      WHERE pl.name = $platform
    """
      .query[TrackedProduct]
      .to[List]
      .transact(xa)

}
