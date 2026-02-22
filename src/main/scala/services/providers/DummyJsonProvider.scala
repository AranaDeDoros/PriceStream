package org.aranadedoros.pricestream
package services.providers

import domain.dto.*
import domain.models.{Platform, *}
import cats.effect.*
import org.http4s.*
import org.http4s.Method.*
import org.http4s.circe.*
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import org.http4s.client.*
import org.http4s.implicits.uri

class DummyJsonProvider(
  client: Client[IO],
  platform: Platform
) extends JsonProvider(client, platform)
    with ProductProvider:

  override val baseUri = uri"https://dummyjson.com"

  override def fetchProducts(): IO[List[TrackedProduct]] =
    client.expect[DummyProductsResponse](
      Request[IO](GET, baseUri / "products")
    ).map {
      response =>
        response.products.map {
          dto =>
            TrackedProduct(
              id = 0L, // DB lo genera
              platformId = platform.id,
              externalId = dto.id.toString,
              name = Some(dto.title),
              url = Some(s"$baseUri/products/${dto.id}")
            )
        }
    }

  override def fetchPrice(externalId: String): IO[Option[Price]] =
    client.expectOption[DummyProductDto](
      Request[IO](GET, baseUri / "products" / externalId)
    ).map {
      case Some(dto) => Some(Price(dto.price))
      case None      => None
    }
