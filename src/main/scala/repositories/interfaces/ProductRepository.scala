package org.aranadedoros.pricestream
package repositories.interfaces

import cats.effect.IO
import org.aranadedoros.pricestream.domain.models.TrackedProduct

trait ProductRepository {
  def findByExternalId(platformId: Long, externalId: String): IO[Option[TrackedProduct]]
  def findLatest(n: Int) : IO[Seq[TrackedProduct]]
  def insert(product: TrackedProduct): IO[TrackedProduct]
}
