package routes

import cats.effect.{IO, Ref}
import munit.CatsEffectSuite

import org.aranadedoros.pricestream.domain.dto.*
import org.aranadedoros.pricestream.domain.errors.TrackingError
import org.aranadedoros.pricestream.domain.models.{PriceUpdate, TrackedProduct}
import org.aranadedoros.pricestream.routes.TrackingRoutes
import org.aranadedoros.pricestream.services.interfaces.TrackingService

import org.http4s.Method.{GET, POST}
import org.http4s.implicits.*
import org.http4s.{Request, Uri}
import org.http4s.circe.CirceEntityCodec._
import io.circe.Json
import java.time.Instant

class TrackingRoutesSpec extends CatsEffectSuite {

  private class StubTrackingService(
    trackPriceResult: Either[TrackingError, Unit],
    historyResult: Either[TrackingError, List[PriceUpdate]],
    productsResult: List[TrackedProduct],
    observedPlatform: Ref[IO, Option[Option[String]]]
  ) extends TrackingService[IO] {

    override def trackPrice(
      platform: String,
      externalId: String,
      price: BigDecimal,
      name: Option[String],
      url: Option[String]
    ): IO[Either[TrackingError, Unit]] = IO.pure(trackPriceResult)

    override def getHistory(
      platform: String,
      externalId: String
    ): IO[Either[TrackingError, List[PriceUpdate]]] = IO.pure(historyResult)

    override def listProducts(platform: Option[String]): IO[List[TrackedProduct]] =
      observedPlatform.set(Some(platform)) *> IO.pure(productsResult)
  }

  test("POST /track returns 201 when finding a tracked run") {
    for {
      observed <- Ref.of[IO, Option[Option[String]]](None)
      service = new StubTrackingService(
        trackPriceResult = Right(()),
        historyResult = Right(Nil),
        productsResult = Nil,
        observedPlatform = observed
      )
      request = Request[IO](POST, uri"/track").withEntity(
        TrackPriceRequest("amazon", "SKU-1", BigDecimal("199.99"), Some("Kindle"), Some("https://example.com"))
      )
      response <- new TrackingRoutes[IO](service).httpRoutes.orNotFound.run(request)
    } yield assertEquals(response.status.code, 201)
  }

  test("POST /track 400 for invalid prices") {
    for {
      observed <- Ref.of[IO, Option[Option[String]]](None)
      service = new StubTrackingService(
        trackPriceResult = Left(TrackingError.InvalidPrice(BigDecimal("-1"))),
        historyResult = Right(Nil),
        productsResult = Nil,
        observedPlatform = observed
      )
      request = Request[IO](POST, uri"/track").withEntity(
        TrackPriceRequest("amazon", "SKU-1", BigDecimal("-1"), None, None)
      )
      response <- new TrackingRoutes[IO](service).httpRoutes.orNotFound.run(request)
      body     <- response.as[String]
    } yield {
      assertEquals(response.status.code, 400)
      assert(body.contains("Invalid price"))
    }
  }

  test("GET /history/{platform}/{externalId} returns a history") {
    val recordedAt = Instant.parse("2024-01-03T10:00:00Z")

    for {
      observed <- Ref.of[IO, Option[Option[String]]](None)
      service = new StubTrackingService(
        trackPriceResult = Right(()),
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
        trackPriceResult = Right(()),
        historyResult = Left(TrackingError.ProductNotFound("amazon", "SKU-404")),
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
        trackPriceResult = Right(()),
        historyResult = Right(Nil),
        productsResult = List(TrackedProduct(1L, 5L, "SKU-1", Some("Kindle"), Some("https://example.com"))),
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
