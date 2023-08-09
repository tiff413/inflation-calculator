package v1.calculate

import javax.inject.Inject

import play.api.Logger
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

case class CalculateFormInput(title: String, body: String)

/** Takes HTTP requests and produces JSON.
  */
class CalculateController @Inject()(cc: CalculateControllerComponents)(implicit
                                                                       ec: ExecutionContext
) extends CalculateBaseController(cc) {

  private val logger = Logger(getClass)

  private val form: Form[CalculateFormInput] = {
    import play.api.data.Forms._

    Form(
      mapping(
        "title" -> nonEmptyText,
        "body" -> text
      )(CalculateFormInput.apply)(CalculateFormInput.unapply)
    )
  }

  def index: Action[AnyContent] = CalculateAction.async { implicit request =>
    logger.trace("index: ")
    CalculateResourceHandler.find.map { posts =>
      Ok(Json.toJson(posts))
    }
  }

  def process: Action[AnyContent] = CalculateAction.async { implicit request =>
    logger.trace("process: ")
    processJsonPost()
  }

  def show(id: String): Action[AnyContent] = CalculateAction.async {
    implicit request =>
      logger.trace(s"show: id = $id")
      CalculateResourceHandler.lookup(id).map { post =>
        Ok(Json.toJson(post))
      }
  }

  private def processJsonPost[A]()(implicit
      request: PostRequest[A]
  ): Future[Result] = {
    def failure(badForm: Form[CalculateFormInput]) = {
      Future.successful(BadRequest(badForm.errorsAsJson))
    }

    def success(input: CalculateFormInput) = {
      CalculateResourceHandler.create(input).map { post =>
        Created(Json.toJson(post)).withHeaders(LOCATION -> post.link)
      }
    }

    form.bindFromRequest().fold(failure, success)
  }
}
