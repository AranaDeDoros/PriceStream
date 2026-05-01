package org.aranadedoros.pricestream
package repositories

import domain.models.{IngestionRun, IngestionStatus}
import repositories.interfaces.LoggerRepository

import cats.effect.IO
import doobie.*
import doobie.implicits.*
import doobie.postgres.implicits.*

import java.time.Instant
import java.util.UUID

class DoobieLoggerRepository(xa: Transactor[IO]) extends LoggerRepository:

  override def insert(run: IngestionRun): IO[Unit] =
    sql"""
     INSERT INTO ingestion_runs (id, started_at, finished_at, status, error)
    VALUES (
      ${run.id},
      ${run.startedAt},
      ${run.finishedAt},
      ${IngestionStatus.toString(run.status)},
      ${run.error}
    )
    """
      .update
      .run
      .transact(xa)
      .void

  override def update(id: UUID, status: IngestionStatus): IO[Unit] =
    sql"""
      UPDATE ingestion_runs
      SET finished_at = NOW(),
          status = ${IngestionStatus.toString(status)}
      WHERE id = $id
    """
      .update
      .run
      .transact(xa)
      .void

  def updateWithError(id: UUID, status: IngestionStatus, error: String): IO[Unit] =
    sql"""
      UPDATE ingestion_runs
      SET finished_at = NOW(),
          status = ${IngestionStatus.toString(status)},
          error = $error
      WHERE id = $id
    """
      .update
      .run
      .transact(xa)
      .void

  override def all: IO[Seq[IngestionRun]] =
    sql"""
      SELECT id, started_at, finished_at, status, error
      FROM ingestion_runs
      ORDER BY started_at DESC
    """
      .query[(UUID, Instant, Option[Instant], String, Option[String])]
      .map {
        case (id, started, finished, statusStr, error) =>
          IngestionRun(
            id,
            started,
            finished,
            IngestionStatus.fromString(statusStr),
            error
          )
      }
      .to[Seq]
      .transact(xa)
