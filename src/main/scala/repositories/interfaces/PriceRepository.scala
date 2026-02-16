package org.aranadedoros.pricestream
package repositories.interfaces

import cats.effect.IO
import org.aranadedoros.pricestream.domain.models.Price

trait PriceRepository {
  def findLatestPrice(productId: Long): IO[Option[Price]]
  def insert(productId: Long, price: Price): IO[Unit]
}
