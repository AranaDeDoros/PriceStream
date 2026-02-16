package org.aranadedoros.pricestream
package services

import cats.effect.IO
import org.aranadedoros.pricestream.domain.models.Platform
import org.aranadedoros.pricestream.repositories.interfaces.PlatformRepository

class PlatformProviderService(
                             repo: PlatformRepository
                             ) {

  def all : IO[Seq[Platform]] = repo.all
  def ids  : IO[Seq[Long]] = repo.ids
}
