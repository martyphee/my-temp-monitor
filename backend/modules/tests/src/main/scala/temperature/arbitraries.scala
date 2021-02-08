package temperature

import com.martyphee.temperature.domain.Reading.ReadingParam
import com.martyphee.temperature.domain.TemperatureEvent.CreateTemperatureEvent
import io.estatico.newtype.Coercible
import io.estatico.newtype.ops._
import org.scalacheck.{Arbitrary, Gen}
import temperature.generators._

import java.util.UUID

object arbitraries {

  implicit def arbCoercibleInt[A: Coercible[Int, *]]: Arbitrary[A] =
    Arbitrary(Gen.posNum[Int].map(_.coerce[A]))

  implicit def arbCoercibleStr[A: Coercible[String, *]]: Arbitrary[A] =
    Arbitrary(cbStr[A])

  implicit def arbCoercibleUUID[A: Coercible[UUID, *]]: Arbitrary[A] =
    Arbitrary(cbUuid[A])

  implicit val arbBrand: Arbitrary[ReadingParam] =
    Arbitrary(readingGen)

  implicit val arbCreateTemperatureReading: Arbitrary[CreateTemperatureEvent] =
    Arbitrary(temperatureEventGen)
}
