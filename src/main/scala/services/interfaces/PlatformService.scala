package org.aranadedoros.pricestream
package services.interfaces

import cats.effect.IO
import org.aranadedoros.pricestream.domain.models.Platform

trait PlatformService {
  def all : IO[Seq[Platform]]
}
