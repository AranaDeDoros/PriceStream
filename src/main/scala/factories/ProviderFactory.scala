
package org.aranadedoros.pricestream
package factories
import domain.models.*
import services.providers.{DummyJsonProvider, ProductProvider}

import cats.effect.IO
import org.http4s.client.Client

object ProviderFactory :

  def create(client: Client[IO], platform: Platform): Option[ProductProvider] =
    PlatformProvider.fromString(platform.name).toOption.map {
      case PlatformProvider.DummyJson =>
        new DummyJsonProvider(client, platform)
    }



