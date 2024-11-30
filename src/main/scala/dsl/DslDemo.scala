import api.AlphaVantageClient

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}
import akka.actor.ActorSystem
import akka.stream.Materializer
import dsl.{LimitPrice, Portfolio, Quantity, Stock, StockOption}


object DslDemo extends App {

  // Create ActorSystem and Materializer (implicitly required by Akka HTTP)
  implicit val system: ActorSystem = ActorSystem("alpha-vantage-system")
  implicit val materializer: Materializer = Materializer(system)
  implicit val ec: ExecutionContext = system.dispatcher // ExecutionContext for handling futures

  // API key for Alpha Vantage
  val apiKey = "6L4F5FO8C8QOMQRN" // Replace with your Alpha Vantage API key

  // Instantiate AlphaVantageClient with implicit parameters
  val alphaVantageClient = new AlphaVantageClient(apiKey)

  // Instantiate Portfolio and pass the AlphaVantageClient instance as a dependency
  private val portfolio = Portfolio(alphaVantageClient)
    .buy(Stock("AAPL"), Quantity(100))
    .sell(StockOption("AAPL", "CALL", "2024-12-31", 150), Quantity(10))
    .buy(Stock("GOOGL"), Quantity(50), Some(LimitPrice(2800)))
    .marketBuy(Stock("GOOGL"), Quantity(50))
    .marketSell(Stock("AMZN"), Quantity(20))

  // Print out portfolio orders
  println(portfolio.getOrders)
  println()
  portfolio.show()
  println()

  // Simulate market order executions with real-time price fetching
  portfolio.executeMarketOrders()  // Execute market orders
  println()

  // The stock symbol for which we want to fetch the real-time price
  val symbol = "AAPL"  // Replace with my desired stock symbol

  // Fetch real-time price using the AlphaVantageClient
  alphaVantageClient.getRealTimePrice(symbol).onComplete {
    case Success(priceOpt) =>
      println(s"The latest price for $symbol is: ${priceOpt.getOrElse("Unavailable")}")
    case Failure(exception) =>
      println(s"Failed to fetch price: $exception")
  }

  // Example of interacting with the portfolio, you can add buy/sell orders
  portfolio.buy("AAPL", Quantity(10)) // Adding a buy order for AAPL

  // Show the portfolio orders
  portfolio.show()
  println()
}