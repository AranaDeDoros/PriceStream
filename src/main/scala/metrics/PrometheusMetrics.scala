package org.aranadedoros.pricestream
package metrics

import cats.data.OptionT
import cats.effect.Async
import cats.syntax.all.*
import org.http4s.HttpRoutes
import io.prometheus.client.{CollectorRegistry, Counter, Histogram}

/** Process-wide application metrics. The registry is deliberately owned here, rather than by the server bootstrap, so
  * metrics remain an application concern.
  */
object PrometheusMetrics:

  private val registry = new CollectorRegistry()

  private val httpRequests = Counter
    .build()
    .name("pricestream_http_requests_total")
    .help("Completed HTTP requests handled by PriceStream")
    .labelNames("route", "method", "status")
    .register(registry)

  private val httpDuration = Histogram
    .build()
    .name("pricestream_http_request_duration_seconds")
    .help("Time spent handling HTTP requests in seconds")
    .labelNames("route", "method", "status")
    .register(registry)

  private val ingestionRuns = Counter
    .build()
    .name("pricestream_ingestion_runs_total")
    .help("Completed ingestion runs")
    .labelNames("outcome")
    .register(registry)

  private val ingestedProducts = Counter
    .build()
    .name("pricestream_ingested_products_total")
    .help("Products processed by successful ingestion runs")
    .register(registry)

  def instrumentHttpRoutes[F[_]: Async](
    routeName: String,
    routes: HttpRoutes[F]
  ): HttpRoutes[F] =
    HttpRoutes[F] {
      request =>
        OptionT {
          for
            started <- Async[F].monotonic
            result  <- routes.run(request).value
            ended   <- Async[F].monotonic
            _ <- result.traverse_ {
              response =>
                Async[F].delay {
                  val status  = response.status.code.toString
                  val seconds = (ended - started).toNanos.toDouble / 1e9
                  httpRequests.labels(routeName, request.method.name, status).inc()
                  httpDuration.labels(routeName, request.method.name, status).observe(seconds)
                }
            }
          yield result
        }
    }

  def recordIngestionSuccess(productsProcessed: Int): Unit =
    ingestionRuns.labels("success").inc()
    ingestedProducts.inc(productsProcessed.toDouble)

  def recordIngestionFailure(): Unit =
    ingestionRuns.labels("failure").inc()

  def collectorRegistry: CollectorRegistry = registry
