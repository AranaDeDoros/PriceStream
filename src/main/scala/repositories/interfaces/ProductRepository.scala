package org.aranadedoros.pricestream
package repositories.interfaces

import domain.models.TrackedProduct

import cats.effect.IO

trait ProductRepository:
  def findByExternalId(platformId: Long, externalId: String): IO[Option[TrackedProduct]]
  def findLatest(n: Int): IO[Seq[TrackedProduct]]
  def insert(product: TrackedProduct): IO[TrackedProduct]
