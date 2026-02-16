package org.aranadedoros.pricestream
package modules

import cats.effect.IO
import cats.implicits.catsSyntaxApplicativeId
import doobie.Transactor
import org.aranadedoros.pricestream.repositories.DoobieIngestionRepository
import org.aranadedoros.pricestream.services.ExternalAPIService

object ExternalAPIModule {
  def make(
                         xa: Transactor[IO],
                       ): IO[ExternalAPIService] = {
    val repo = DoobieIngestionRepository(xa)
    val svc  = ExternalAPIService(repo)
    svc.pure
  }
}
