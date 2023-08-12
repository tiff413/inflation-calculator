package v1.calculation

import java.util.Date

trait CalculationRepository {
  def calculateInflation(input: InputQuery): Either[String, OutputQuery]
}

@Singleton
class CalculationRepositoryImpl extends CalculationRepository {
  override def calculateInflation(input: InputQuery): Either[String, OutputQuery] = {
    // test
    Right(OutputQuery(
      InputQuery(
        new Date(1000),
        new Date(1000),
        BigDecimal.long2bigDecimal(10)
      ),
      BigDecimal.long2bigDecimal(20)
    ))
  }
}
