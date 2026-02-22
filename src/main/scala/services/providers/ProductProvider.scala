package org.aranadedoros.pricestream
package services.providers

import domain.models.{Price, TrackedProduct}

import cats.effect.IO

trait ProductProvider:
  def fetchProducts(): IO[Seq[TrackedProduct]]
  def fetchPrice(externalId: String): IO[Option[Price]]
