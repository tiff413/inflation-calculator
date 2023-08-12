package v1.calculation

import javax.inject.Inject
import play.api.Logger
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

/** Takes HTTP requests and produces JSON.
  */
@Singleton
class CalculationController @Inject() (ccc: CalculationControllerComponents)(
    implicit ec: ExecutionContext
) extends CalculationBaseController(ccc) {

  private val logger = Logger(getClass)

  private val form: Form[InputQuery] = {
    import play.api.data.Forms._

    Form(
      mapping(
        "startDate" -> date,
        "endDate" -> date,
        "price" -> bigDecimal
      )(InputQuery.apply)(InputQuery.unapply)
    )
  }

  def process: Action[AnyContent] = CalculationAction.async { implicit request =>
    logger.trace("process: ")
    processJson()
  }

  private def processJson[A]()(implicit
      request: CalculationRequest[A]
  ): Future[Result] = {
    def failure(badForm: Form[InputQuery]) = {
      Future.successful(BadRequest(badForm.errorsAsJson))
    }

    def success(input: InputQuery) = {
      // what to do after POST req
      ccc.calculationRepository.calculateInflation(input) match {
        case Left(errorMsg) => Future.successful(BadRequest(errorMsg))
        case Right(outputQuery) => Ok(Json.toJson(outputQuery))
      }

    }

    form.bindFromRequest().fold(failure, success)
  }
}
