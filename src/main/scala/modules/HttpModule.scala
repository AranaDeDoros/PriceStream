package org.aranadedoros.pricestream
package modules

import routes.TrackingRoutes
import services.interfaces.TrackingService

import cats.effect.Async
import org.http4s.HttpRoutes

object HttpModule:

  def routes[F[_]: Async](
    trackingSvc: TrackingService[F]
  ): HttpRoutes[F] =
    TrackingRoutes[F](trackingSvc).httpRoutes
