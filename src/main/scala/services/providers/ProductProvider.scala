package org.aranadedoros.pricestream
package services.providers

import cats.effect.IO
import org.aranadedoros.pricestream.domain.models.{Price, TrackedProduct}
import org.http4s.Uri

trait ProductProvider {
  def fetchProducts(): IO[Seq[TrackedProduct]]
  def fetchPrice(externalId: String): IO[Option[Price]]
}

