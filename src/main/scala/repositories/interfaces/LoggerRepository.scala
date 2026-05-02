package org.aranadedoros.pricestream
package repositories.interfaces

import domain.models.{IngestionRun, IngestionStatus}

import cats.effect.IO

import java.util.UUID

trait LoggerRepository:
  def insert(run: IngestionRun): IO[Unit]
  def update(id: UUID, status: IngestionStatus): IO[Unit]
  def updateWithSummary(id: UUID, status: IngestionStatus, count: Int): IO[Unit]
  def updateWithError(id: UUID, status: IngestionStatus, error: String): IO[Unit]
  def all: IO[Seq[IngestionRun]]
