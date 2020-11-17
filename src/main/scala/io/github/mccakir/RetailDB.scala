package io.github.mccakir

import akka.actor.{Actor, ActorLogging}
import io.github.mccakir.RetailDB._


case class Product(productId: Int, productName: String, productPrice: Double, categoryId: Int)

object RetailDB {

  case class CreateProduct(product: Product)

  case class ProductCreated(productId: Int)

  case class FindProductById(productId: Int)

  case object FindAllGuitars

}

class RetailDB extends Actor with ActorLogging {
  var products: Map[Int, Product] = Map()

  override def receive: Receive = {
    case FindAllGuitars =>
      log.info("Searching for all products")
      sender() ! products.values.toList
    case FindProductById(productId) =>
      log.info(s"Searching product by id $productId")
      sender() ! products.get(productId)
    case CreateProduct(product) =>
      log.info(s"Adding product $product with id ${product.productId}")
      products = products + (product.productId -> product)
      sender() ! ProductCreated(product.productId)
  }
}
