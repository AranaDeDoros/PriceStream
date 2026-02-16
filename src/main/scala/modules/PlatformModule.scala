package org.aranadedoros.pricestream
package modules

import domain.models.Platform
import repositories.interfaces.{DoobiePlatformRepository, PlatformRepository}
import services.PlatformProviderService
import cats.effect.IO
import doobie.hikari.HikariTransactor

object PlatformModule {

  def fetchAll(xa: HikariTransactor[IO]) : IO[Seq[Platform]] =
    val repo: PlatformRepository = DoobiePlatformRepository(xa)
    val svc = PlatformProviderService(repo)
    svc.all
    
  def fetchIds(xa: HikariTransactor[IO]) : IO[Seq[Long]]=
    val repo: PlatformRepository = DoobiePlatformRepository(xa)
    val svc = PlatformProviderService(repo)
    svc.ids
}
