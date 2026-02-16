package org.aranadedoros.pricestream
package repositories.interfaces

import cats.effect.IO
import org.aranadedoros.pricestream.domain.models.Platform

trait PlatformRepository {
  def all: IO[Seq[Platform]]
  def ids: IO[Seq[Long]]
}
