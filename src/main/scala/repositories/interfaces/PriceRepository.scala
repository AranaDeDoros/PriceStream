package org.aranadedoros.pricestream
package repositories.interfaces

import domain.models.Price

import cats.effect.IO

trait PriceRepository:
  def findLatestPrice(productId: Long): IO[Option[Price]]
  def insert(productId: Long, price: Price): IO[Unit]
