package org.aranadedoros.pricestream
package modules

import repositories.DoobieTrackingRepository
import services.LiveTrackingService
import services.interfaces.TrackingService

import cats.effect.Async
import cats.implicits.catsSyntaxApplicativeId
import doobie.Transactor

object TrackingModule:
  def make[F[_]: Async](
    xa: Transactor[F]
  ): F[TrackingService[F]] = {
    val repo = DoobieTrackingRepository[F](xa)
    val svc  = LiveTrackingService[F](repo)
    svc.pure[F]
  }
