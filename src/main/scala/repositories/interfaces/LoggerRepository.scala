package org.aranadedoros.pricestream
package repositories.interfaces

import cats.effect.IO
import org.aranadedoros.pricestream.domain.models.{IngestionRun, IngestionStatus}

import java.util.UUID

trait LoggerRepository:
  def insert(run: IngestionRun): IO[Unit]
  def update(id: UUID, status: IngestionStatus): IO[Unit]
  def all: IO[Seq[IngestionRun]]

