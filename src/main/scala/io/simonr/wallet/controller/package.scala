package io.simonr.wallet

import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder
import io.simonr.wallet.model.UserId

package object controller {
  trait JsonBase

  case class Error(message: String) extends AnyVal

  case class Balance(balance: BigDecimal) extends AnyVal

  trait JsonEncoders {
    implicit lazy val errorEncoder: Encoder[Error] = deriveEncoder
    implicit lazy val balanceEncoder: Encoder[Balance] = deriveEncoder
  }

  object PlayerIdValidator {
    def parse(value: String): UserId = UserId(value)
  }

}
