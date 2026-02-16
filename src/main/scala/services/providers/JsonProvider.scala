package org.aranadedoros.pricestream
package services.providers

import cats.effect.IO
import org.aranadedoros.pricestream.domain.models.Platform
import org.http4s.Uri
import org.http4s.client.Client

abstract class  JsonProvider(
                              client: Client[IO],
                              platform: Platform,
                            ) {
  val baseUri: Uri
}