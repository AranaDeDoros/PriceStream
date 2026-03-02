package org.aranadedoros.pricestream
package repositories

import domain.models.Platform
import cats.effect.IO

trait PlatformRepository:
  def getPlatforms: IO[Seq[Platform]]
