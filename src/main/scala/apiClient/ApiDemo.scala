

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}
import akka.actor.ActorSystem
import akka.stream.Materializer

import dsl.{Portfolio, Quantity, Stock, StockOption}


object ApiDemo extends App {

  // Create ActorSystem and Materializer (implicitly required by Akka HTTP)
  implicit val system: ActorSystem = ActorSystem("alpha-vantage-system")
  implicit val materializer: Materializer = Materializer(system)
  implicit val ec: ExecutionContext = system.dispatcher // ExecutionContext for handling futures

  // API key for Alpha Vantage
  // standard API rate limit is 25 requests per day.
  val apiKey = "6L4F5FO8C8QOMQRN" // Replace with your Alpha Vantage API key

  // Instantiate AlphaVantageClient with implicit parameters
  val alphaVantageClient = new AlphaVantageClient(apiKey)


  // The stock symbol for which we want to fetch the real-time price
  val symbol = "AAPL"  // Replace with my desired stock symbol

  // Fetch real-time price using the AlphaVantageClient
  alphaVantageClient.getRealTimePrice(symbol).onComplete {
    case Success(priceOpt) =>
      println(s"The latest price for $symbol is: ${priceOpt.getOrElse("Unavailable")}")
      system.terminate()  // Terminate ActorSystem once the task completes
    case Failure(exception) =>
      println(s"Failed to fetch price: $exception")
      system.terminate()  // Terminate ActorSystem in case of failure
  }

  // Ensure the ActorSystem doesn't immediately shut down before the task completes
  system.whenTerminated.onComplete {
    case _ => println("ActorSystem terminated.")
  }
}