package org.aranadedoros.pricestream
package repositories

import domain.models
import domain.models.{CatalogueProduct, Platform, PriceUpdate, TrackedPriceRecord, TrackingStatuses}
import domain.dto.TrackPriceResponse
import repositories.interfaces.TrackingRepository

import cats.effect.Async
import cats.syntax.all.*
import doobie.Transactor
import doobie.implicits.*
import doobie.postgres.implicits.*
import doobie.util.Get

import java.time.Instant

class DoobieTrackingRepository[F[_]: Async](
  xa: Transactor[F]
) extends TrackingRepository[F]:

  given Get[TrackingStatuses] =
    Get[String].temap(
      TrackingStatuses.fromString
    )
    
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
  def findProduct(platformId: Long, externalId: String): F[Option[CatalogueProduct]] =
    sql"""
      SELECT id, platform_id, external_id, name, url
      FROM products
      WHERE platform_id = $platformId
        AND external_id = $externalId
    """.query[CatalogueProduct]
      .option
      .transact(xa)

  def createProduct(
    platformId: Long,
    externalId: String,
    name: Option[String],
    url: Option[String]
  ): F[CatalogueProduct] =
    sql"""
      INSERT INTO products (platform_id, external_id, name, url)
      VALUES ($platformId, $externalId, $name, $url)
      RETURNING id, platform_id, external_id, name, url
    """.query[CatalogueProduct]
      .unique
      .transact(xa)

  // Price
  override def insertPrice(productId: Long, price: BigDecimal): F[Unit] =
    sql"""
      INSERT INTO price_history (product_id, price)
      VALUES ($productId, $price)
    """.update
      .run
      .transact(xa)
      .void

  override def getPriceHistory(productId: Long): F[List[PriceUpdate]] =
    sql"""
      SELECT price, recorded_at
      FROM price_history
      WHERE product_id = $productId
      ORDER BY recorded_at ASC
    """.query[PriceUpdate]
      .to[List]
      .transact(xa)

  override def listProducts: F[List[CatalogueProduct]] =
    sql"""
      SELECT pr.id, pr.platform, pr.external_id, pr.name, pr.url
      FROM products pr
    """
      .query[CatalogueProduct]
      .to[List]
      .transact(xa)

  override def listProductsByPlatform(platform: String): F[List[CatalogueProduct]] =
    sql"""
      SELECT pr.id, pr.platform_id, pr.external_id, pr.name, pr.url
      FROM products pr
      JOIN platforms pl ON pl.id = pr.platform_id
      WHERE pl.name = $platform
    """
      .query[CatalogueProduct]
      .to[List]
      .transact(xa)


  override def insertTrackingRequest(platform: Platform, product: CatalogueProduct) : F[TrackPriceResponse] =

    val (platform_id, tracked_product_id, tracked_product_price) = 
        (platform.id, product.id, product.price)
    val status = TrackingStatuses.Tracking.toString
    val trackedAt = Instant.now()

    sql"""
      INSERT INTO tracking_requests (tracked_product_id, tracked_product_price, platform_id, status, tracked_at)
      VALUES ($tracked_product_id, $tracked_product_price, $platform_id, $status, $trackedAt)
      RETURNING status, tracked_at
    """.query[TrackPriceResponse]
      .unique
      .transact(xa)

  override def getTrackingPriceHistory(url: String)  : F[List[TrackedPriceRecord]] =
    sql"""
        SELECT  p."name", ph.price, pl."name" AS platform, ph.recorded_at, tr.tracked_product_price AS tracked_price
        FROM price_history ph
        JOIN products p
        ON p.id  = ph.product_id
        JOIN platforms pl
        ON p.platform = pl.id
        JOIN tracking_requests tr
        ON tr.tracked_product_id = p.id
        JOIN tracking_urls tu
        ON tu.tracking_request_id  = tr.id
        WHERE tr.status = 'Tracking'
        AND tu.url = $url
    """.query[TrackedPriceRecord]
      .to[List]
      .transact(xa)
