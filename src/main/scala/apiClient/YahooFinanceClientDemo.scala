package apiClient

import akka.actor.ActorSystem
import akka.stream.Materializer

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object YahooFinanceClientDemo extends App {

  /*
  API key for YahooFinanceClient
  Due to Yahoo licence issue, I can't get data from this API.
 */
    //API key for YahooFinanceClient
    // Instantiate YahooFinanceClient
  // Create ActorSystem and Materializer (implicitly required by Akka HTTP)
  implicit val system: ActorSystem = ActorSystem("yahoo-system")
  implicit val materializer: Materializer = Materializer(system)
  implicit val ec: ExecutionContext = system.dispatcher

  private val yahooFinanceClient = new YahooFinanceClient()

    // Example: Fetch real-time price for AAPL
    val symbol = "AAPL"

    yahooFinanceClient.getRealTimePrice(symbol).onComplete {
      case Success(Some(price)) =>
        println(s"The current price of $symbol is $$ $price")
      case Success(None) =>
        println(s"Could not fetch the price for $symbol.")
      case Failure(exception) =>
        println(s"Failed to fetch price: $exception")
    }
}
