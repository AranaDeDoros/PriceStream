package org.aranadedoros.pricestream
package repositories

import domain.models.Price
import repositories.interfaces.PriceRepository
import cats.effect.IO
import doobie.*
import doobie.implicits.*

class DoobiePriceRepository(xa: Transactor[IO]) extends PriceRepository:

  override def findLatestPrice(productId: Long): IO[Option[Price]] =
    sql"""
      SELECT price
      FROM price_history
      WHERE product_id = $productId
      ORDER BY recorded_at DESC
      LIMIT 1
    """
      .query[Double]
      .option
      .map(_.map(Price.apply))
      .transact(xa)

  override def insert(productId: Long, price: Price): IO[Unit] =
    sql"""
      INSERT INTO price_history (product_id, price)
      VALUES ($productId, ${price.amount})
    """
      .update
      .run
      .transact(xa)
      .void
