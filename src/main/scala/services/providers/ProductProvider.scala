package org.aranadedoros.pricestream
package services.providers

import domain.models.{CatalogueProduct, Price}

import cats.effect.IO

trait ProductProvider:
  def fetchProducts(): IO[Seq[CatalogueProduct]]
  def fetchPrice(externalId: String): IO[Option[Price]]
