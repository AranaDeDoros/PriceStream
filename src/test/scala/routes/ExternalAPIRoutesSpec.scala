package routes

import cats.effect.IO
import munit.CatsEffectSuite
import org.aranadedoros.pricestream.domain.models.{IngestionRun, IngestionStatus}
import org.aranadedoros.pricestream.repositories.interfaces.IngestRepository
import org.aranadedoros.pricestream.routes.ExternalAPIRoutes
import org.aranadedoros.pricestream.services.ExternalAPIService
import org.http4s.Method.GET
import org.http4s.{Request, Uri}
import org.http4s.implicits.*

import java.time.Instant
import java.util.UUID

class ExternalAPIRoutesSpec extends CatsEffectSuite {

  private val runId: UUID = UUID.fromString("11111111-1111-1111-1111-111111111111")
  private val ingestionRun = IngestionRun(
    id = runId,
    startedAt = Instant.parse("2024-01-01T00:00:00Z"),
    finishedAt = Some(Instant.parse("2024-01-01T00:05:00Z")),
    status = IngestionStatus.Completed
  )

  private val ingestRepo = new IngestRepository {
    override def all: IO[Seq[IngestionRun]] = IO.pure(Seq(ingestionRun))

    override def findById(id: UUID): IO[Option[IngestionRun]] =
      IO.pure(Option.when(id == runId)(ingestionRun))

    override def findByStatus(status: IngestionStatus): IO[Seq[IngestionRun]] =
      IO.pure(Seq(ingestionRun).filter(_.status == status))
  }

  private val httpApp = ExternalAPIRoutes.routes(new ExternalAPIService(ingestRepo)).orNotFound

  test("GET /runs returns a list") {
    val request = Request[IO](method = GET, uri = uri"/runs")

    for {
      response <- httpApp.run(request)
      body     <- response.as[String]
    } yield {
      assertEquals(response.status.code, 200)
      assert(body.contains(runId.toString))
      assert(body.contains("\"status\":\"Completed\""))
    }
  }

  test("GET /runs/{id} returns an existing run") {
    val request = Request[IO](method = GET, uri = Uri.unsafeFromString(s"/runs/$runId"))

    for {
      response <- httpApp.run(request)
      body     <- response.as[String]
    } yield {
      assertEquals(response.status.code, 200)
      assert(body.contains(runId.toString))
    }
  }

  test("GET /runs/{id} returns 404 for non-existent runs") {
    val missingId = UUID.fromString("22222222-2222-2222-2222-222222222222")
    val request   = Request[IO](method = GET, uri = Uri.unsafeFromString(s"/runs/$missingId"))

    httpApp.run(request).map {
      response =>
        assertEquals(response.status.code, 404)
    }
  }

  test("GET /status/{status} return a list of runs by state") {
    val request = Request[IO](method = GET, uri = uri"/status/Completed")

    for {
      response <- httpApp.run(request)
      body     <- response.as[String]
    } yield {
      assertEquals(response.status.code, 200)
      assert(body.contains("\"status\":\"Completed\""))
    }
  }

  test("GET /status/{status} returns 404 when no runs by this status") {
    val request = Request[IO](method = GET, uri = uri"/status/Failed")

    httpApp.run(request).map {
      response =>
        assertEquals(response.status.code, 404)
    }
  }
}
