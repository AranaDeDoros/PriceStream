package org.aranadedoros.pricestream
package repositories

import domain.models.CatalogueProduct
import repositories.interfaces.ProductRepository

import cats.effect.IO
import doobie.*
import doobie.implicits.*

class DoobieProductRepository(xa: Transactor[IO]) extends ProductRepository:

  override def findByExternalId(
    platformId: Long,
    externalId: String
  ): IO[Option[CatalogueProduct]] =
    sql"""
      SELECT id, platform, external_id, name, url, price
      FROM products
      WHERE platform = $platformId
      AND external_id = $externalId
    """
      .query[CatalogueProduct]
      .option
      .transact(xa)

  override def insert(product: CatalogueProduct): IO[CatalogueProduct] =
    sql"""
      INSERT INTO products (platform, external_id, name, url, price)
      VALUES (${product.platformId}, ${product.externalId}, ${product.name}, ${product.url}, ${product.price})
      RETURNING id, platform, external_id, name, url, price
    """
      .query[CatalogueProduct]
      .unique
      .transact(xa)

  // GET  /products/latest
  override def findLatest(
    n: Int
  ): IO[Seq[CatalogueProduct]] =
    sql"""
        SELECT id, platform, external_id, name, url, price
        FROM products
        ORDER BY id DESC
        LIMIT $n
      """
      .query[CatalogueProduct]
      .to[Seq]
      .transact(xa)
