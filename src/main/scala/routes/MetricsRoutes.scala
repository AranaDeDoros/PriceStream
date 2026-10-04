package org.aranadedoros.pricestream
package routes

import cats.effect.Sync
import cats.syntax.all.*
import org.http4s.*
import org.http4s.dsl.Http4sDsl
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.exporter.common.TextFormat
import java.io.StringWriter

object MetricsRoutes:

  def routes[F[_]: Sync](registry: CollectorRegistry): HttpRoutes[F] =
    object dsl extends Http4sDsl[F]
    import dsl.*

    HttpRoutes.of[F] {
      case GET -> Root / "metrics" =>
        Sync[F].blocking {
          val writer = new StringWriter()
          TextFormat.write004(writer, registry.metricFamilySamples())
          writer.toString
        }.flatMap {
          body =>
            Ok(body).map(
              _.putHeaders(
                Header.Raw(
                  org.typelevel.ci.CIString("Content-Type"),
                  TextFormat.CONTENT_TYPE_004
                )
              )
            )
        }
    }
