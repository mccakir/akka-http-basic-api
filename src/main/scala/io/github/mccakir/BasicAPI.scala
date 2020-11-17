package io.github.mccakir

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import spray.json._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

trait ProductJsonProtocol extends DefaultJsonProtocol {
  implicit val productFormat = jsonFormat4(Product)
}

object BasicAPI extends ProductJsonProtocol {

  def main(args: Array[String]): Unit = {

    implicit val system = ActorSystem("BasicAPI")
    implicit val materializer = ActorMaterializer()
    import RetailDB._
    import akka.http.scaladsl.server.Directives._
    import system.dispatcher

    // RetailDB setup
    val retailDB = system.actorOf(Props[RetailDB], "RetailDB")
    val productList = List(
      Product(1, "macbook pro", 10000, 1),
      Product(2, "iphone", 5000, 1),
      Product(3, "ipad", 3000, 1)
    )
    productList.foreach { product =>
      retailDB ! CreateProduct(product)
    }

    implicit val timeout = Timeout(2 seconds)

    val serverRoute: Route =
      (pathPrefix("api" / "product") & get) {
        (path(IntNumber) | parameter(Symbol("productId").as[Int])) { productId =>
          val productFeature: Future[Option[Product]] = ((retailDB ? FindProductById(productId)))
            .mapTo[Option[Product]]
          val entityFeature = productFeature.map { productOption =>
            HttpEntity(
              ContentTypes.`application/json`,
              productOption.toJson.prettyPrint
            )
          }
          complete(entityFeature)
        } ~
          pathEndOrSingleSlash {
            val productsFeature: Future[List[Product]] = (retailDB ? FindAllGuitars).mapTo[List[Product]]
            val entityFeature = productsFeature.map { product =>
              HttpEntity(
                ContentTypes.`application/json`,
                product.toJson.prettyPrint
              )
            }
            complete(entityFeature)
          }
      }

    Http().bindAndHandle(serverRoute, "localhost", 8080)
  }

}


