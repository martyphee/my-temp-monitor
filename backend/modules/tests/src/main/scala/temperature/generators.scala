package temperature

import com.martyphee.temperature.domain.Reading._
import com.martyphee.temperature.domain.TemperatureEvent._
import io.estatico.newtype.Coercible
import io.estatico.newtype.ops._
import org.scalacheck._
import Gen._
import com.martyphee.temperature.domain.TemperatureEvent.EventType._
import squants.market._

import java.math.MathContext
import java.util.UUID

object generators {
  def cbUuid[A: Coercible[UUID, *]]: Gen[A] =
    Gen.uuid.map(_.coerce[A])

  def cbStr[A: Coercible[String, *]]: Gen[A] =
    genNonEmptyString.map(_.coerce[A])

  def cbInt[A: Coercible[Int, *]]: Gen[A] =
    Gen.posNum[Int].map(_.coerce[A])

  def cbDec[A: Coercible[BigDecimal, *]]: Gen[A] =
    Gen.posNum[BigDecimal].map(_.coerce[A])

  val scale = 3

  def genDecimal[A: Coercible[BigDecimal, *]]: Gen[A] =
    for {
      n <- Gen.choose[BigDecimal](-100, 100)
    } yield n.round(new MathContext(scale)).coerce[A]

  val genMoney: Gen[Money] =
    Gen.posNum[Long].map(n => USD(BigDecimal(n)))

  val genNonEmptyString: Gen[String] =
    Gen
      .chooseNum(21, 40)
      .flatMap(n => Gen.buildableOfN[String, Char](n, Gen.alphaChar))

  def chooseType: Gen[EventType] =
    Gen.oneOf(const(FanOn), const(FanOff))

  val readingGen: Gen[ReadingParam] =
    for {
      i <- genDecimal[ReadingTemperature]
    } yield ReadingParam(i)

  val temperatureEventGen: Gen[CreateTemperatureEvent] =
    for {
      e <- chooseType
    } yield CreateTemperatureEvent(e)

}
