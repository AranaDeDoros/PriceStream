package org.aranadedoros.pricestream
package repositories.interfaces

import domain.models.Platform

import cats.effect.IO

trait PlatformRepository:
  def all: IO[Seq[Platform]]
  def ids: IO[Seq[Long]]
