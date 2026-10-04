package org.aranadedoros.pricestream
package routes

import domain.dto.*
import domain.errors.TrackingError
import domain.models.{CatalogueProduct, Platform, PriceUpdate, TrackedPriceRecord, TrackingStatuses}
import services.interfaces.TrackingService
import cats.effect.{IO, Ref}
import io.circe.Json
import munit.CatsEffectSuite
import org.http4s.Method.{GET, POST}
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.implicits.*
import org.http4s.{Request, Uri}

import java.time.Instant

class TrackingRoutesSpec extends CatsEffectSuite {

  private class StubTrackingService(
    trackPriceResult: Either[TrackingError, TrackPriceResponse],
    historyResult: Either[TrackingError, List[PriceUpdate]],
    productsResult: List[CatalogueProduct],
    observedPlatform: Ref[IO, Option[Option[String]]],
    trackingHistoryResult: List[TrackedPriceRecord] = Nil
  ) extends TrackingService[IO] {

    override def trackPrice(
      request: TrackPriceRequest
    ): IO[Either[TrackingError, TrackPriceResponse]] = IO.pure(trackPriceResult)

    override def getHistory(
      platform: String,
      externalId: String
    ): IO[Either[TrackingError, List[PriceUpdate]]] = IO.pure(historyResult)

    override def listProducts(platform: Option[String]): IO[List[CatalogueProduct]] =
      observedPlatform.set(Some(platform)) *> IO.pure(productsResult)

    override def getTrackingRequestHistory(url: String): IO[List[TrackedPriceRecord]] =
      IO.pure(trackingHistoryResult)
  }

  test("POST /track returns 201 with a short tracking URL") {
    val trackedAt   = Instant.parse("2024-01-03T10:00:00Z")
    val trackingUrl = "aB3dE5fG"

    for {
      observed <- Ref.of[IO, Option[Option[String]]](None)

      service = new StubTrackingService(
        trackPriceResult = Right(
          TrackPriceResponse(
            status = TrackingStatuses.Tracking,
            trackedAt = trackedAt,
            trackingUrl = trackingUrl
          )
        ),
        historyResult = Right(Nil),
        productsResult = Nil,
        observedPlatform = observed
      )

      request = Request[IO](POST, uri"/track").withEntity(
        TrackPriceRequest("amazon", "SKU-1")
      )

      response <- new TrackingRoutes[IO](service)
        .httpRoutes
        .orNotFound
        .run(request)

      body <- response.as[TrackPriceResponse]

    } yield {
      assertEquals(response.status.code, 201)
      assertEquals(body.status, TrackingStatuses.Tracking)
      assertEquals(body.trackedAt, trackedAt)
      assertEquals(body.trackingUrl, trackingUrl)
    }
  }

  test("GET /track/requests/{trackingUrl} returns the tracking request history") {
    val recordedAt  = Instant.parse("2024-01-03T10:00:00Z")
    val trackingUrl = "aB3dE5fG"
    val record = TrackedPriceRecord(
      name = "Kindle",
      price = BigDecimal("149.50"),
      platform = Platform(5L, "amazon", "https://amazon.example"),
      recordedAt = recordedAt,
      trackedPrice = BigDecimal("159.99")
    )

    for {
      observed <- Ref.of[IO, Option[Option[String]]](None)
      service = new StubTrackingService(
        trackPriceResult = Right(TrackPriceResponse(trackedAt = recordedAt, trackingUrl = trackingUrl)),
        historyResult = Right(Nil),
        productsResult = Nil,
        observedPlatform = observed,
        trackingHistoryResult = List(record)
      )
      request = Request[IO](GET, Uri.unsafeFromString(s"/track/requests/$trackingUrl"))
      response <- new TrackingRoutes[IO](service).httpRoutes.orNotFound.run(request)
      body     <- response.as[Json]
    } yield {
      assertEquals(response.status.code, 200)
      val firstRecord = body.hcursor.downArray
      assertEquals(firstRecord.get[String]("name"), Right("Kindle"))
      assertEquals(firstRecord.get[BigDecimal]("price"), Right(BigDecimal("149.50")))
      assertEquals(firstRecord.get[String]("recordedAt"), Right(recordedAt.toString))
      assertEquals(firstRecord.get[BigDecimal]("trackedPrice"), Right(BigDecimal("159.99")))
    }
  }

  test("GET /history/{platform}/{externalId} returns a history") {
    val recordedAt = Instant.parse("2024-01-03T10:00:00Z")

    for {
      observed <- Ref.of[IO, Option[Option[String]]](None)
      service = new StubTrackingService(
        trackPriceResult = Right(TrackPriceResponse(trackedAt = recordedAt, trackingUrl = "aB3dE5fG")),
        historyResult = Right(List(PriceUpdate(BigDecimal("149.50"), recordedAt))),
        productsResult = Nil,
        observedPlatform = observed
      )
      request = Request[IO](GET, Uri.unsafeFromString("/history/amazon/SKU-1"))
      response <- new TrackingRoutes[IO](service).httpRoutes.orNotFound.run(request)
      body     <- response.as[Json]
    } yield {
      assertEquals(response.status.code, 200)
      val firstRecord = body.hcursor.downArray
      assertEquals(firstRecord.get[BigDecimal]("price"), Right(BigDecimal("149.50")))
      assertEquals(firstRecord.get[String]("recordedAt"), Right(recordedAt.toString))
    }
  }

  test("GET /history/{platform}/{externalId} returns 404 if history doesn't exist") {
    for {
      observed <- Ref.of[IO, Option[Option[String]]](None)
      service = new StubTrackingService(
        trackPriceResult = Right(TrackPriceResponse(trackedAt = Instant.EPOCH, trackingUrl = "aB3dE5fG")),
        historyResult = Left(TrackingError.ProductNotFound("SKU-404")),
        productsResult = Nil,
        observedPlatform = observed
      )
      request = Request[IO](GET, Uri.unsafeFromString("/history/amazon/SKU-404"))
      response <- new TrackingRoutes[IO](service).httpRoutes.orNotFound.run(request)
    } yield assertEquals(response.status.code, 404)
  }

  test("GET /products returns products and normalizes platform") {
    for {
      observed <- Ref.of[IO, Option[Option[String]]](None)
      service = new StubTrackingService(
        trackPriceResult = Right(TrackPriceResponse(trackedAt = Instant.EPOCH, trackingUrl = "aB3dE5fG")),
        historyResult = Right(Nil),
        productsResult =
          List(CatalogueProduct(1L, 5L, "SKU-1", Some("Kindle"), Some("https://example.com"), BigDecimal("149.50"))),
        observedPlatform = observed
      )
      request = Request[IO](GET, uri"/products?platform=AMAZON")
      response     <- new TrackingRoutes[IO](service).httpRoutes.orNotFound.run(request)
      body         <- response.as[Json]
      seenPlatform <- observed.get
    } yield {
      assertEquals(response.status.code, 200)
      assertEquals(seenPlatform, Some(Some("amazon")))
      val firstProduct = body.hcursor.downArray
      assertEquals(firstProduct.get[String]("platform"), Right("5"))
      assertEquals(firstProduct.get[String]("externalId"), Right("SKU-1"))
    }
  }
}
