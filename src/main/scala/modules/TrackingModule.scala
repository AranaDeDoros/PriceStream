package org.aranadedoros.pricestream
package modules

import cats.effect.Async
import cats.implicits.catsSyntaxApplicativeId
import doobie.Transactor
import org.aranadedoros.pricestream.repositories.DoobieTrackingRepository
import org.aranadedoros.pricestream.services.LiveTrackingService
import org.aranadedoros.pricestream.services.interfaces.TrackingService

object TrackingModule {
  def make[F[_]: Async](
                         xa: Transactor[F]
                       ): F[TrackingService[F]] = {
    val repo = DoobieTrackingRepository[F](xa)
    val svc  = LiveTrackingService[F](repo)
    svc.pure[F]
  }
}
