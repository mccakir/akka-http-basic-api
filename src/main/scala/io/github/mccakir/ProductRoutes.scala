package io.github.mccakir

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import io.github.mccakir.ProductRegistry._
import spray.json.{DefaultJsonProtocol, _}

import scala.concurrent.Future
import scala.concurrent.duration._

trait ProductJsonProtocol extends DefaultJsonProtocol {
  implicit val productFormat = jsonFormat4(Product)
}

class ProductRoutes(system: ActorSystem[_], productCRUDActor: ActorRef[ProductRegistry.ProductCRUD]) extends SprayJsonSupport
  with ProductJsonProtocol {
  lazy val log = system.log
  implicit val executionContext = system.executionContext

  implicit val timeout = Timeout(5.seconds) // usually we'd obtain the timeout from the system's configuration
  implicit val scheduler = system.scheduler

  lazy val productRoutes: Route =
    pathPrefix("api" / "product") {
      get {
        (path(IntNumber) | parameter(Symbol("productId").as[Int])) { productId =>
          val productFeature: Future[ProductResponse] = productCRUDActor ? (replyTo => FindProductById(productId, replyTo))
          val entityFeature = productFeature.map { pr =>
            HttpEntity(
              ContentTypes.`application/json`,
              pr.product.toJson.prettyPrint
            )
          }
          complete(entityFeature)
        } ~
          pathEndOrSingleSlash {
            val productsFeature: Future[ProductsResponse] = productCRUDActor ? (replyTo => FindAllProducts(replyTo))
            val entityFeature = productsFeature.map { pr =>
              HttpEntity(
                ContentTypes.`application/json`,
                pr.product.toJson.prettyPrint
              )
            }
            complete(entityFeature)
          }
      } ~
        post {
          entity(as[Product]) { product =>
            val productFeature: Future[ProductCreated] = productCRUDActor ? (replyTo => CreateProduct(product, replyTo))
            val entityFeature = productFeature.map { pc =>
              HttpEntity(
                ContentTypes.`application/json`,
                pc.createdMessage.toJson.prettyPrint
              )
            }
            onSuccess(entityFeature) { entity =>
              complete(StatusCodes.Created, entity)
            }
          }
        } ~
        delete {
          (path(IntNumber) | parameter(Symbol("productId").as[Int])) { productId =>
            val productFeature: Future[ProductDeleteResponse] = productCRUDActor ? (replyTo => DeleteProduct(productId, replyTo))
            val entityFeature = productFeature.map { pds =>
              HttpEntity(
                ContentTypes.`application/json`,
                pds.deletedMessage.toJson.prettyPrint
              )
            }
            onSuccess(entityFeature) { performed =>
              complete(StatusCodes.OK, performed)
            }
          }
        }
    }
}


