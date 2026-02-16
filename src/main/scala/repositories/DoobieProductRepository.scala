package org.aranadedoros.pricestream
package repositories

import cats.effect.IO
import doobie.*
import doobie.implicits.*
import org.aranadedoros.pricestream.domain.models.TrackedProduct
import org.aranadedoros.pricestream.repositories.interfaces.ProductRepository

class DoobieProductRepository(xa: Transactor[IO]) extends ProductRepository {

  override def findByExternalId(
                                 platformId: Long,
                                 externalId: String
                               ): IO[Option[TrackedProduct]] =
    sql"""
      SELECT id, platform, external_id, name, url
      FROM products
      WHERE platform = $platformId
      AND external_id = $externalId
    """
      .query[TrackedProduct]
      .option
      .transact(xa)

  override def insert(product: TrackedProduct): IO[TrackedProduct] =
    sql"""
      INSERT INTO products (platform, external_id, name, url)
      VALUES (${product.platformId}, ${product.externalId}, ${product.name}, ${product.url})
      RETURNING id, platform, external_id, name, url
    """
      .query[TrackedProduct]
      .unique
      .transact(xa)

  //GET  /products/latest
  override def findLatest(
                                 n: Int
                               ): IO[Seq[TrackedProduct]] =
    sql"""
        SELECT id, platform, external_id, name, url
        FROM products
        ORDER BY id DESC
        LIMIT $n
      """
      .query[TrackedProduct]
      .to[Seq]
      .transact(xa)
}

