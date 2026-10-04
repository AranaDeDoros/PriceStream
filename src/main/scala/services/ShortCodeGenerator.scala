package org.aranadedoros.pricestream
package services

import java.security.SecureRandom

object ShortCodeGenerator:
  private val Alphabet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
  private val Random   = new SecureRandom()

  def generate(length: Int = 8): String =
    val bytes = new Array[Byte](length)
    Random.nextBytes(bytes)
    val chars = for b <- bytes yield Alphabet((b & 0xFF) % Alphabet.length)
    new String(chars)