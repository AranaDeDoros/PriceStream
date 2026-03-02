package org.aranadedoros.pricestream
package modules

import repositories.interfaces.DoobiePlatformRepository
import services.PlatformProviderService
import cats.effect.IO
import cats.implicits.catsSyntaxApplicativeId
import doobie.hikari.HikariTransactor

object PlatformModule:
  def make(xa: HikariTransactor[IO]): IO[PlatformProviderService] =
    val repo = DoobiePlatformRepository(xa)
    val svc  = PlatformProviderService(repo)
    svc.pure
