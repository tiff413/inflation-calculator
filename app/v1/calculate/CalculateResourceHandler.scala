package v1.calculate

import javax.inject.{Inject, Provider}

import play.api.MarkerContext

import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json._

/** DTO for displaying calculate information.
  */
case class PostResource(id: String, link: String, title: String, body: String)

object PostResource {

  /** Mapping to read/write a PostResource out as a JSON value.
    */
  implicit val format: Format[PostResource] = Json.format
}

/** Controls access to the backend data, returning [[PostResource]]
  */
class CalculateResourceHandler @Inject() (
    routerProvider: Provider[CalculateRouter],
    calculateRepository: CalculateRepository
)(implicit ec: ExecutionContext) {

  def create(
      postInput: CalculateFormInput
  )(implicit mc: MarkerContext): Future[PostResource] = {
    val data = PostData(PostId("999"), postInput.title, postInput.body)
    // We don't actually create the calculate, so return what we have
    calculateRepository.create(data).map { id =>
      createPostResource(data)
    }
  }

  def lookup(
      id: String
  )(implicit mc: MarkerContext): Future[Option[PostResource]] = {
    val postFuture = calculateRepository.get(PostId(id))
    postFuture.map { maybePostData =>
      maybePostData.map { postData =>
        createPostResource(postData)
      }
    }
  }

  def find(implicit mc: MarkerContext): Future[Iterable[PostResource]] = {
    calculateRepository.list().map { postDataList =>
      postDataList.map(postData => createPostResource(postData))
    }
  }

  private def createPostResource(p: PostData): PostResource = {
    PostResource(p.id.toString, routerProvider.get.link(p.id), p.title, p.body)
  }

}
