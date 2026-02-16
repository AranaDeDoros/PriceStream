package org.aranadedoros.pricestream
package domain.models

import io.circe.Codec
import io.circe.generic.semiauto.*

import java.time.Instant
import scala.math.BigDecimal

final case class Price(amount: Double) extends AnyVal
