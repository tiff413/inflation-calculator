package v1.calculation

import javax.inject.Inject

import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

/**
  * Routes and URLs to the CalculateResource controller.
  */
class CalculationRouter @Inject()(controller: CalculationController) extends SimpleRouter {
  val prefix = "/v1/calculation"

//  def link(id: PostId): String = {
//    import io.lemonlabs.uri.typesafe.dsl._
//    val url = prefix / id.toString
//    url.toString()
//  }

  override def routes: Routes = {
//    case GET(p"/") =>p

    case POST(p"/") =>
      controller.process

//    case GET(p"/$id") =>
//      controller.show(id)
  }

}
