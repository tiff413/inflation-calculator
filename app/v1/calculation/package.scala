package v1

import java.util.Date

package object calculation {
  case class InputQuery(
      startDate: Date,
      endDate: Date,
      price: BigDecimal
  )

  case class OutputQuery(
      input: InputQuery,
      price: BigDecimal
  )
}
