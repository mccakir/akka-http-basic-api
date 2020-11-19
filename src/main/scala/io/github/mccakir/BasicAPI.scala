package io.github.mccakir

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior, PostStop}
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding

import scala.util.{Failure, Success}

object BasicAPI {

  sealed trait Message

  private final case class StartFailed(cause: Throwable) extends Message

  private final case class Started(binding: ServerBinding) extends Message

  case object Stop extends Message


  def main(args: Array[String]): Unit = {
    val productList: Map[Int, ProductRegistry.Product] = Map(
      (1 -> ProductRegistry.Product(1, "macbook pro", 10000, 1)),
      (2 -> ProductRegistry.Product(2, "iphone", 5000, 1)),
      (3 -> ProductRegistry.Product(3, "ipad", 3000, 1))
    )
    val system: ActorSystem[Message] =
      ActorSystem(BasicAPI("localhost", 8080, productList), "BuildJobsServer")

  }

  def apply(host: String, port: Int, products: Map[Int, ProductRegistry.Product]): Behavior[Message] = Behaviors
    .setup {
      ctx =>
        implicit val system = ctx.system
        val productRouteRef = ctx.spawn(ProductRegistry(products), "retailDbTypedActor")
        val routes = new ProductRoutes(system, productRouteRef)
        val serverBinding = Http().newServerAt(host, port).bind(routes.productRoutes)

        ctx.pipeToSelf(serverBinding) {
          case Success(binding) => Started(binding)
          case Failure(ex) => StartFailed(ex)
        }

        def running(binding: ServerBinding): Behavior[Message] =
          Behaviors.receiveMessagePartial[Message] {
            case Stop =>
              ctx.log.info(
                "Stopping server http://{}:{}/",
                binding.localAddress.getHostString,
                binding.localAddress.getPort)
              Behaviors.stopped
          }.receiveSignal {
            case (_, PostStop) =>
              binding.unbind()
              Behaviors.same
          }

        def starting(wasStopped: Boolean): Behaviors.Receive[Message] =
          Behaviors.receiveMessage[Message] {
            case StartFailed(cause) =>
              throw new RuntimeException("Server failed to start", cause)
            case Started(binding) =>
              ctx.log.info(
                "Server online at http://{}:{}/",
                binding.localAddress.getHostString,
                binding.localAddress.getPort)
              if (wasStopped) ctx.self ! Stop
              running(binding)
            case Stop =>
              // we got a stop message but haven't completed starting yet,
              // we cannot stop until starting has completed
              starting(wasStopped = true)
          }

        starting(wasStopped = false)
    }

}
