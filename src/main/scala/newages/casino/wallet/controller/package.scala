package newages.casino.wallet

import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder
import io.circe.syntax._
import newages.casino.wallet.model.PlayerId

package object controller {
  trait JsonBase

  case class Error(message: String) extends AnyVal

  case class Balance(amount: BigDecimal) extends AnyVal

  trait JsonEncoders {
//    implicit lazy val jsonBaseEncoder: Encoder[JsonBase] = Encoder.instance {
//      case obj: Balance => obj.asJson
//    }
    implicit lazy val errorEncoder: Encoder[Error] = deriveEncoder
    implicit lazy val balanceEncoder: Encoder[Balance] = deriveEncoder
  }

  object PlayerIdValidator {
    def parse(value: String): PlayerId = PlayerId(value)
  }

}
