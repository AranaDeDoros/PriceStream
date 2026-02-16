package org.aranadedoros.pricestream
package services

import cats.effect.IO
import org.aranadedoros.pricestream.domain.models.{IngestionRun, IngestionRunDTO, IngestionStatus}
import org.aranadedoros.pricestream.repositories.interfaces.IngestRepository

import java.util.UUID

class ExternalAPIService(
                          ingestRepo: IngestRepository,

                        ) {
  //for fastapi
  def all: IO[Seq[IngestionRun]] = ingestRepo.all
  def findById(id: UUID): IO[Option[IngestionRun]] = ingestRepo.findById(id)
  def findByStatus(status: IngestionStatus): IO[Seq[IngestionRun]] = ingestRepo.findByStatus(status)
  def allDTO: IO[Seq[IngestionRunDTO]] =
    ingestRepo.all.map(_.map(IngestionRunDTO.fromDomain))
  def findByIdDTO(id: UUID): IO[Option[IngestionRunDTO]] =
    ingestRepo.findById(id).map(_.map(IngestionRunDTO.fromDomain))

}
