package org.aranadedoros.pricestream
package services

import domain.models.{IngestionRun, IngestionRunDTO, IngestionStatus}
import repositories.interfaces.IngestRepository
import cats.effect.IO

import java.util.UUID

class ExternalAPIService(
  ingestRepo: IngestRepository
):
  // for fastapi
  def all: IO[Seq[IngestionRun]]                                   = ingestRepo.all
  def findById(id: UUID): IO[Option[IngestionRun]]                 = ingestRepo.findById(id)
  def findByStatus(status: IngestionStatus): IO[Seq[IngestionRun]] = ingestRepo.findByStatus(status)
  def allDTO: IO[Seq[IngestionRunDTO]] =
    ingestRepo.all.map(_.map(IngestionRunDTO.fromDomain))
  def findByIdDTO(id: UUID): IO[Option[IngestionRunDTO]] =
    ingestRepo.findById(id).map(_.map(IngestionRunDTO.fromDomain))
  def findRunsByPlatform(platform: String): IO[Seq[IngestionRun]] =
    ingestRepo.findRunsByPlatform(platform)
