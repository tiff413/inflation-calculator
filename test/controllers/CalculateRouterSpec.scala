import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.http.HttpRequestHandler
import play.api.libs.json.{JsResult, Json}
import play.api.mvc.{Request, RequestHeader, Result}
import play.api.test._
import play.api.test.Helpers._
import play.api.test.CSRFTokenHelper.CSRFRequest
import v1.calculate.PostResource

import scala.concurrent.Future

class CalculateRouterSpec extends PlaySpec with GuiceOneAppPerTest {

  "CalculateRouter" should {

    "render the list of posts" in {
      val request = FakeRequest(GET, "/v1/posts").withHeaders(HOST -> "localhost:9000")
      val home:Future[Result] = route(app, request).get

      val posts: Seq[PostResource] = Json.fromJson[Seq[PostResource]](contentAsJson(home)).get
      posts.filter(_.id == "1").head mustBe (PostResource("1","/v1/posts/1", "title 1", "blog calculate 1" ))
    }

    "render the list of posts when url ends with a trailing slash" in {
      val request = FakeRequest(GET, "/v1/posts/").withHeaders(HOST -> "localhost:9000").withCSRFToken
      val home:Future[Result] = route(app, request).get

      val posts: Seq[PostResource] = Json.fromJson[Seq[PostResource]](contentAsJson(home)).get
      posts.filter(_.id == "1").head mustBe (PostResource("1","/v1/posts/1", "title 1", "blog calculate 1" ))
    }
  }

}