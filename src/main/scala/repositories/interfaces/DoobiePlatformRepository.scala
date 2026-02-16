package org.aranadedoros.pricestream
package repositories.interfaces

import domain.models.Platform
import cats.effect.IO
import doobie.*
import doobie.implicits.*

class DoobiePlatformRepository(xa: Transactor[IO]) extends PlatformRepository {

  override def all: IO[Seq[Platform]] =
    sql"""
      SELECT id, name, url
      FROM platforms
    """
      .query[Platform]
      .to[Seq]
      .transact(xa)

  override def ids: IO[Seq[Long]] =
    sql"""
      SELECT id
      FROM platforms
    """
      .query[Long]
      .to[Seq]
      .transact(xa)

}

