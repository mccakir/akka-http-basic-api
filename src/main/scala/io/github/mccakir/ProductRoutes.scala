package io.github.mccakir

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import io.github.mccakir.RetailDbTyped._
import spray.json.{DefaultJsonProtocol, _}

import scala.concurrent.Future
import scala.concurrent.duration._

trait ProductJsonProtocol extends DefaultJsonProtocol {
  implicit val productFormat = jsonFormat4(Product)
}

class ProductRoutes(system: ActorSystem[_], productCRUDActor: ActorRef[RetailDbTyped.ProductCRUD]) extends ProductJsonProtocol {
  lazy val log = system.log
  implicit val executionContext = system.executionContext

  implicit val timeout = Timeout(5.seconds) // usually we'd obtain the timeout from the system's configuration
  implicit val scheduler = system.scheduler

  lazy val productRoutes: Route =
    pathPrefix("api" / "product") {
      pathEnd {
        get {
          val products: Future[ProductsResponse] = productCRUDActor ? (replyTo => FindAllProducts(replyTo))
          val entityFeature = products.map { pr =>
            HttpEntity(
              ContentTypes.`application/json`,
              pr.product.toJson.prettyPrint
            )
          }
          complete(entityFeature)
        }
      }
    }
}


