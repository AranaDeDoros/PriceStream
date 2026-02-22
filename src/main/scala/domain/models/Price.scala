package org.aranadedoros.pricestream
package domain.models

import io.circe.generic.semiauto.*

final case class Price(amount: Double) extends AnyVal
