package org.aranadedoros.pricestream
package repositories.interfaces

import domain.models.CatalogueProduct

import cats.effect.IO

trait ProductRepository:
  def findByExternalId(platformId: Long, externalId: String): IO[Option[CatalogueProduct]]
  def findLatest(n: Int): IO[Seq[CatalogueProduct]]
  def insert(product: CatalogueProduct): IO[CatalogueProduct]
