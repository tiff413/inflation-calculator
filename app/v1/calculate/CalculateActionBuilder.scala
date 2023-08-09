package v1.calculate

import javax.inject.Inject

import net.logstash.logback.marker.LogstashMarker
import play.api.{Logger, MarkerContext}
import play.api.http.{FileMimeTypes, HttpVerbs}
import play.api.i18n.{Langs, MessagesApi}
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

/** A wrapped request for calculate resources.
  *
  * This is commonly used to hold request-specific information like
  * security credentials, and useful shortcut methods.
  */
trait PostRequestHeader
    extends MessagesRequestHeader
    with PreferredMessagesProvider
class PostRequest[A](request: Request[A], val messagesApi: MessagesApi)
    extends WrappedRequest(request)
    with PostRequestHeader

/** Provides an implicit marker that will show the request in all logger statements.
  */
trait RequestMarkerContext {
  import net.logstash.logback.marker.Markers

  private def marker(tuple: (String, Any)) = Markers.append(tuple._1, tuple._2)

  private implicit class RichLogstashMarker(marker1: LogstashMarker) {
    def &&(marker2: LogstashMarker): LogstashMarker = marker1.and(marker2)
  }

  implicit def requestHeaderToMarkerContext(implicit
      request: RequestHeader
  ): MarkerContext = {
    MarkerContext {
      marker("id" -> request.id) && marker("host" -> request.host) && marker(
        "remoteAddress" -> request.remoteAddress
      )
    }
  }

}

/** The action builder for the Calculate resource.
  *
  * This is the place to put logging, metrics, to augment
  * the request with contextual data, and manipulate the
  * result.
  */
class CalculateActionBuilder @Inject() (
    messagesApi: MessagesApi,
    playBodyParsers: PlayBodyParsers
)(implicit val executionContext: ExecutionContext)
    extends ActionBuilder[PostRequest, AnyContent]
    with RequestMarkerContext
    with HttpVerbs {

  override val parser: BodyParser[AnyContent] = playBodyParsers.anyContent

  type CalculateRequestBlock[A] = PostRequest[A] => Future[Result]

  private val logger = Logger(this.getClass)

  override def invokeBlock[A](
      request: Request[A],
      block: CalculateRequestBlock[A]
  ): Future[Result] = {
    // Convert to marker context and use request in block
    implicit val markerContext: MarkerContext = requestHeaderToMarkerContext(
      request
    )
    logger.trace(s"invokeBlock: ")

    val future = block(new PostRequest(request, messagesApi))

    future.map { result =>
      request.method match {
        case GET | HEAD =>
          result.withHeaders("Cache-Control" -> s"max-age: 100")
        case other =>
          result
      }
    }
  }
}

/** Packages up the component dependencies for the calculate controller.
  *
  * This is a good way to minimize the surface area exposed to the controller, so the
  * controller only has to have one thing injected.
  */
case class CalculateControllerComponents @Inject() (
                                                     postActionBuilder: CalculateActionBuilder,
                                                     postResourceHandler: CalculateResourceHandler,
                                                     actionBuilder: DefaultActionBuilder,
                                                     parsers: PlayBodyParsers,
                                                     messagesApi: MessagesApi,
                                                     langs: Langs,
                                                     fileMimeTypes: FileMimeTypes,
                                                     executionContext: scala.concurrent.ExecutionContext
) extends ControllerComponents

/** Exposes actions and handler to the CalculateController by wiring the injected state into the base class.
  */
class CalculateBaseController @Inject()(ccc: CalculateControllerComponents)
    extends BaseController
    with RequestMarkerContext {
  override protected def controllerComponents: ControllerComponents = ccc

  def CalculateAction: CalculateActionBuilder = ccc.postActionBuilder

  def CalculateResourceHandler: CalculateResourceHandler = ccc.postResourceHandler
}
