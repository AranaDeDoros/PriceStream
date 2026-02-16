package org.aranadedoros.pricestream

import com.comcast.ip4s.{ipv4, port}
import com.typesafe.config.ConfigFactory
import modules.*
import routes.ExternalAPIRoutes
import cats.effect.{IO, IOApp, Resource}
import cats.syntax.all.*
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory
import scala.concurrent.duration.*

object Main extends IOApp.Simple:

  given LoggerFactory[IO] = Slf4jFactory.create[IO]
  private val config = ConfigFactory.load()
  private def transactor: Resource[IO, HikariTransactor[IO]] =
    for
      ce <- ExecutionContexts.fixedThreadPool[IO](32)
      xa <- HikariTransactor.newHikariTransactor[IO](
        config.getString("app.db.driver"),
        config.getString("app.db.url"),
        config.getString("app.db.user"),
        config.getString("app.db.passw"),
        ce
      )
    yield xa

  override def run: IO[Unit] =
    val logger = LoggerFactory[IO].getLogger
    (transactor, EmberClientBuilder.default[IO].withTimeout(30.seconds).build)
      .tupled
      .use { (xa, client) =>
        val program =
          for {
            platforms <- Resource.eval(PlatformModule.fetchAll(xa))
            trackingSvc    <- Resource.eval(TrackingModule.make[IO](xa))
            externalSvc <- Resource.eval(ExternalAPIModule.make(xa))
            trackingRoutes = HttpModule.routes[IO](trackingSvc)
            externalRoutes = ExternalAPIRoutes.routes(externalSvc)

            httpApp = Router(
              "/tracking" -> trackingRoutes,
              "/api" -> externalRoutes
            ).orNotFound

            server <- EmberServerBuilder
              .default[IO]
              .withHost(ipv4"0.0.0.0")
              .withPort(port"8080")
              .withHttpApp(httpApp)
              .build

            ingest <- IngestModule.make(xa, client, platforms, logger)
          } yield (server, ingest)

        program.useForever
      }

