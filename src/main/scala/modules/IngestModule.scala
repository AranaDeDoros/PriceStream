package org.aranadedoros.pricestream
package modules

import domain.models.{IngestionRun, IngestionStatus, Platform}
import factories.ProviderFactory
import repositories.interfaces.LoggerRepository
import repositories.*
import services.*
import services.providers.{DummyJsonProvider, ProductProvider}
import cats.effect.{IO, Resource}
import cats.syntax.all.*
import doobie.hikari.HikariTransactor
import fs2.Stream
import org.http4s.client.Client
import org.typelevel.log4cats.Logger
import java.util.UUID
import scala.concurrent.duration.*

object IngestModule:

  def make(
    xa: HikariTransactor[IO],
    client: Client[IO],
    platforms: Seq[Platform],
    logger: Logger[IO]
  ): Resource[IO, Unit] = {

    val productRepo = new DoobieProductRepository(xa)
    val priceRepo   = new DoobiePriceRepository(xa)
    val loggerRepo  = new DoobieLoggerRepository(xa)

    val providers: Seq[ProductProvider] =
      platforms.flatMap(
        p => ProviderFactory.create(client, p)
      )

    val ingestService =
      new IngestService(
        providers,
        productRepo,
        priceRepo
      )

    val stream =
      Stream.eval {
        logger.info("Starting initial ingest...") *>
          runTrackedIngestion(ingestService, loggerRepo, logger) *>
          logger.info("Initial ingest completed")
      } ++
        Stream.awakeEvery[IO](10.minutes)
          .evalMap {
            _ =>
              logger.info("Running scheduled ingest...") *>
                runTrackedIngestion(ingestService, loggerRepo, logger) *>
                logger.info("Scheduled ingest completed")
          }

    Resource.make {
      for {
        fiber <- stream.compile.drain.start
        _     <- logger.info("Ingest scheduler started")
      } yield fiber
    } {
      fiber =>
        logger.info("Shutting down ingest scheduler...") *>
          fiber.cancel
    }.void
  }

  private def runTrackedIngestion(
    ingestService: IngestService,
    loggerRepo: LoggerRepository,
    logger: Logger[IO]
  ): IO[Unit] =
    for {
      id  <- IO(UUID.randomUUID())
      now <- IO.realTimeInstant
      _ <- loggerRepo.insert(
        IngestionRun(id, now, None, IngestionStatus.Running)
      )

      result <- ingestService.ingestOnce().attempt

      _ <- result match
        case Right(_) =>
          loggerRepo.update(id, IngestionStatus.Completed)

        case Left(e) =>
          loggerRepo.update(id, IngestionStatus.Failed) *>
            logger.error(e)("Ingestion failed")
    } yield ()
