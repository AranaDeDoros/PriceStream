package org.aranadedoros.pricestream
package modules

import cats.effect.Async
import org.aranadedoros.pricestream.routes.TrackingRoutes
import org.aranadedoros.pricestream.services.interfaces.TrackingService
import org.http4s.HttpRoutes

object HttpModule {

  def routes[F[_]: Async](
                           trackingSvc: TrackingService[F]
                         ): HttpRoutes[F] =
    TrackingRoutes[F](trackingSvc).httpRoutes
}
