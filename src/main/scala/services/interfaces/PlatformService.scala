package org.aranadedoros.pricestream
package services.interfaces

import domain.models.Platform

import cats.effect.IO

trait PlatformService:
  def all: IO[Seq[Platform]]
