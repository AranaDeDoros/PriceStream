package org.aranadedoros.pricestream
package repositories.interfaces

import cats.effect.IO
import org.aranadedoros.pricestream.domain.models.{IngestionRun, IngestionStatus}

import java.util.UUID

trait IngestRepository {
  def all : IO[Seq[IngestionRun]]
  def findById(id: UUID) : IO[Option[IngestionRun]]
  def findByStatus(status: IngestionStatus) : IO[Seq[IngestionRun]]
}
