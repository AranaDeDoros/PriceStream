package org.aranadedoros.pricestream
package domain.models

import doobie.Meta
import io.circe.Encoder
import io.circe.generic.semiauto.*

import java.time.Instant
import java.util.UUID

sealed trait IngestionStatus
object IngestionStatus {
  case object Running   extends IngestionStatus
  case object Completed extends IngestionStatus
  case object Failed    extends IngestionStatus

  def fromString(s: String): IngestionStatus =
    s match
      case "Running" => Running
      case "Completed" => Completed
      case "Failed" => Failed
      case other => sys.error(s"Invalid ingestion status: $other")

  def toString(status: IngestionStatus): String =
    status match
      case Running => "Running"
      case Completed => "Completed"
      case Failed => "Failed"

  given Meta[IngestionStatus] =
    Meta[String].imap(fromString)(toString)
}

case class IngestionRun(
                         id: UUID,
                         startedAt: Instant,
                         finishedAt: Option[Instant],
                         status: IngestionStatus
                       )

object IngestionRun {
  given Encoder[IngestionRun] = deriveEncoder
}

case class IngestionRunDTO(
  id: UUID,
  startedAt: Instant,
  finishedAt: Option[Instant],
  status: String
)

object IngestionRunDTO {
  def fromDomain(run: IngestionRun): IngestionRunDTO =
    IngestionRunDTO(
      id = run.id,
      startedAt = run.startedAt,
      finishedAt = run.finishedAt,
      status = run.status.toString
    )
  given Encoder[IngestionRunDTO] = deriveEncoder
}