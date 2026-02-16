package org.aranadedoros.pricestream
package repositories

import cats.effect.IO
import doobie.*
import doobie.implicits.*
import doobie.postgres.implicits.*
import org.aranadedoros.pricestream.domain.models.{IngestionRun, IngestionStatus}
import org.aranadedoros.pricestream.repositories.interfaces.IngestRepository

import java.util.UUID

class DoobieIngestionRepository(xa: Transactor[IO]) extends IngestRepository {

  override def all: IO[Seq[IngestionRun]] =
    sql"""
      SELECT id, started_at, finished_at, status
      FROM ingestion_runs
    """
      .query[IngestionRun]
      .to[Seq]
      .transact(xa)

  override def findById(id: UUID): IO[Option[IngestionRun]] =
    sql"""
      SELECT id, started_at, finished_at, status
      FROM ingestion_runs
      WHERE id = $id
    """
    .query[IngestionRun]
    .option
    .transact(xa)

  override def findByStatus(status: IngestionStatus): IO[Seq[IngestionRun]] =
    sql"""
         SELECT id, started_at, finished_at, status
         FROM ingestion_runs
         WHERE status = ${status.toString}
       """
      .query[IngestionRun]
      .to[Seq]
      .transact(xa)
}
