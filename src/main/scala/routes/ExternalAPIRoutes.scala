package org.aranadedoros.pricestream
package routes

import domain.models.{IngestionRun, IngestionStatus}
import services.{ExternalAPIService, PlatformProviderService}

import cats.effect.IO
import io.circe.Encoder
import io.circe.syntax.*
import org.http4s.*
import org.http4s.Method.GET
import org.http4s.circe.*
import org.http4s.dsl.impl.*
import org.http4s.dsl.io.*

object ExternalAPIRoutes:

  def routes(svc: ExternalAPIService, platformSvc: PlatformProviderService): HttpRoutes[IO] =
    HttpRoutes.of[IO] {
      case GET -> Ro
        ot / "runs" =>
        svc.allDTO.flatMap(
          runs => Ok(runs.asJson)
        )

      case GET -> Root / "runs" / "platform" / platform =>
        svc.findRunsByPlatform(platform).flatMap {
          runs =>
            if (runs.isEmpty) NotFound()
            else Ok(runs.asJson)
        }

      case GET -> Root / "runs" / UUIDVar(id) =>
        svc.findByIdDTO(id).flatMap {
          case Some(run) => Ok(run.asJson)
          case None      => NotFound()
        }

      case GET -> Root / "status" / status =>
        svc.findByStatus(IngestionStatus.fromString(status)).flatMap {
          runs =>
            if (runs.isEmpty) NotFound()
            else Ok(runs.asJson)
        }

      case GET -> Root / "platforms" =>
        platformSvc.all.flatMap {
          runs =>
            if (runs.isEmpty) NotFound()
            else Ok(runs.asJson)
        }
    }
