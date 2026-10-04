package org.aranadedoros.pricestream
package modules

import metrics.PrometheusMetrics
import routes.{ExternalAPIRoutes, MetricsRoutes, TrackingRoutes}
import services.interfaces.TrackingService
import services.{ExternalAPIService, PlatformProviderService}
import cats.effect.{Async, IO}
import org.http4s.{HttpApp, HttpRoutes}
import org.http4s.server.Router

object HttpModule:

  def routes[F[_]: Async](
    trackingSvc: TrackingService[F]
  ): HttpRoutes[F] =
    TrackingRoutes[F](trackingSvc).httpRoutes

  /** Builds the complete public HTTP application, including the standard Prometheus scrape endpoint at /metrics.
    */
  def app(
    trackingSvc: TrackingService[IO],
    externalSvc: ExternalAPIService,
    platformSvc: PlatformProviderService
  ): HttpApp[IO] =
    Router(
      "/tracking" -> PrometheusMetrics.instrumentHttpRoutes("tracking", routes(trackingSvc)),
      "/api" -> PrometheusMetrics.instrumentHttpRoutes(
        "external_api",
        ExternalAPIRoutes.routes(externalSvc, platformSvc)
      ),
      "/" -> MetricsRoutes.routes(PrometheusMetrics.collectorRegistry)
    ).orNotFound
