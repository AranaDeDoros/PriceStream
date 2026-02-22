package org.aranadedoros.pricestream
package services.providers

import domain.models.Platform

import cats.effect.IO
import org.http4s.Uri
import org.http4s.client.Client

abstract class JsonProvider(
  client: Client[IO],
  platform: Platform
) {
  val baseUri: Uri
}
