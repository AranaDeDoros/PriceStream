package org.aranadedoros.pricestream
package routes

import domain.dto.*
import domain.errors.TrackingError
import domain.models.*
import services.interfaces.TrackingService
import cats.effect.*
import cats.syntax.all.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.dsl.Http4sDsl
import org.http4s.dsl.impl.OptionalQueryParamDecoderMatcher

class TrackingRoutes[F[_]: Async](
  service: TrackingService[F]
) extends Http4sDsl[F]:

  given EntityDecoder[F, TrackPriceRequest]         = jsonOf
  given EntityDecoder[F, TrackPriceResponse]        = jsonOf
  given EntityEncoder[F, List[PriceUpdateResponse]] = jsonEncoderOf

  private def mapError(error: TrackingError): F[Response[F]] =
    error match {
      case TrackingError.InvalidPrice(p) =>
        BadRequest(s"Invalid price: $p")

      case TrackingError.ProductNotFound(id) =>
        NotFound(s"Product $id not found")

      case TrackingError.PlatformNotFound(name) =>
        NotFound(s"Platform $name not found")

      case TrackingError.PersistenceError(msg) =>
        InternalServerError(s"Persistence error: $msg")
    }

  object PlatformQueryParamMatcher
      extends OptionalQueryParamDecoderMatcher[String]("platform")

  val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {

    // POST /tracking/track
    case req @ POST -> Root / "track" =>
      for {
        body <- req.as[TrackPriceRequest]

        result <- service.trackPrice(body)
        response <- result match {
          case Right(resp) => Created(resp)
          case Left(err)   => mapError(err)
        }

      } yield response

    // GET /tracking/track/{tracking_url}
    // a join of multiple tables, returns a List[TrackedPriceRecord]
    case req @ GET -> Root / "tracking" / trackingUrl =>
      service
        .getTrackingRequestHistory(trackingUrl)
        .flatMap {
          history =>
            val response = history
            Ok(response)
        }

    // GET /api/history/{platform}/{externalId}
    case GET -> Root / "history" / platform / externalId =>
      service
        .getHistory(platform, externalId)
        .flatMap {
          case Right(history) =>
            val response =
              history.map(
                h =>
                  PriceUpdateResponse(h.price, h.recordedAt)
              )

            Ok(response)

          case Left(err) =>
            mapError(err)
        }

    // GET /api/products
    // GET /api/products?platform=amazon
    case GET -> Root / "products" :? PlatformQueryParamMatcher(platformOpt) =>
      val normalized = platformOpt.map(_.toLowerCase)
      service.listProducts(normalized).flatMap {
        products =>
          val response =
            products.map {
              p =>
                ProductResponse(
                  platform = p.platformId.toString,
                  externalId = p.externalId,
                  name = p.name,
                  url = p.url
                )
            }

          Ok(response)
      }

  }
