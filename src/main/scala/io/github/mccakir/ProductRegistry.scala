package io.github.mccakir

import akka.actor.typed.scaladsl._
import akka.actor.typed.{ActorRef, Behavior}

object ProductRegistry {

  case class Product(productId: Int, productName: String, productPrice: Double, categoryId: Int)

  case class Products(products: Map[Int, Product])

  trait ProductCRUD

  case class CreateProduct(product: Product, replyTo: ActorRef[ProductCreated]) extends ProductCRUD

  case class FindProductById(productId: Int, replyTo: ActorRef[ProductResponse]) extends ProductCRUD

  case class FindAllProducts(replyTo: ActorRef[ProductsResponse]) extends ProductCRUD

  case class DeleteProduct(productId: Int, replyTo: ActorRef[ProductDeleteResponse]) extends ProductCRUD


  trait ProductReply

  case class ProductCreated(createdMessage: String) extends ProductReply

  case class ProductResponse(product: Option[Product]) extends ProductReply

  case class ProductsResponse(product: List[Product]) extends ProductReply

  case class ProductDeleteResponse(deletedMessage: String) extends ProductReply


  def apply(products: Map[Int, ProductRegistry.Product]): Behavior[ProductCRUD] =
    Behaviors.receiveMessage {
      case CreateProduct(product, replyTo) =>
        println(s"Adding product $product with id ${product.productId}")
        replyTo ! ProductCreated(s"Product with productId: ${product.productId} created successfully")
        ProductRegistry(products + (product.productId -> product))
      case FindProductById(productId, replyTo) =>
        println(s"Searching product by id $productId")
        replyTo ! ProductResponse(products.get(productId))
        Behaviors.same
      case FindAllProducts(replyTo) =>
        println("Searching for all products")
        replyTo ! ProductsResponse(products.values.toList)
        Behaviors.same
      case DeleteProduct(productId, replyTo) =>
        println(s"Deleting product with productId: $productId")
        replyTo ! ProductDeleteResponse(s"Product with productId: ${productId} deleted successfully")
        ProductRegistry(products - productId)
    }

}
