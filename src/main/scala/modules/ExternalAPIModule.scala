package org.aranadedoros.pricestream
package modules

import repositories.DoobieIngestionRepository
import services.ExternalAPIService

import cats.effect.IO
import cats.implicits.catsSyntaxApplicativeId
import doobie.Transactor

object ExternalAPIModule:
  def make(
    xa: Transactor[IO]
  ): IO[ExternalAPIService] = {
    val repo = DoobieIngestionRepository(xa)
    val svc  = ExternalAPIService(repo)
    svc.pure
  }
