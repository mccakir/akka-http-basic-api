package io.github.mccakir

import akka.actor.typed.scaladsl._
import akka.actor.typed.{ActorRef, Behavior}

object RetailDbTyped {

  case class Product(productId: Int, productName: String, productPrice: Double, categoryId: Int)

  case class Products(products: Map[Int, Product])

  trait ProductCRUD

  case class CreateProduct(product: Product, replyTo: ActorRef[ProductCreated]) extends ProductCRUD

  case class FindProductById(productId: Int, replyTo: ActorRef[ProductResponse]) extends ProductCRUD

  case class FindAllProducts(replyTo: ActorRef[ProductsResponse]) extends ProductCRUD


  trait ProductReply

  case class ProductCreated(productId: Int) extends ProductReply

  case class ProductResponse(product: Option[Product]) extends ProductReply

  case class ProductsResponse(product: List[Product]) extends ProductReply


  def apply(products: Map[Int, RetailDbTyped.Product]): Behavior[ProductCRUD] =
    Behaviors.receiveMessage {
      case CreateProduct(product, replyTo) =>
        println(s"Adding product $product with id ${product.productId}")
        replyTo ! ProductCreated(product.productId)
        RetailDbTyped(products + (product.productId -> product))
      case FindProductById(productId, replyTo) =>
        println(s"Searching product by id $productId")
        replyTo ! ProductResponse(products.get(productId))
        Behaviors.same
      case FindAllProducts(replyTo) =>
        replyTo ! ProductsResponse(products.values.toList)
        Behaviors.same
    }

}
