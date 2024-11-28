import TradingApp.system
import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.http.scaladsl.Http

import scala.concurrent.ExecutionContext
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

import java.time.Instant
import java.time.temporal.ChronoUnit
import java.time.format.DateTimeFormatter
import java.time.ZoneId

object TradingApp extends App{
  println("Hello, TradingApp!")
  //User Methods to Create Buy/Sell Orders
  // Creates a Portfolio and adds several orders (buy and sell).
  // It shows the portfolio's orders with the getOrder or show method.

  // Define implicit ActorSystem, Materializer, and ExecutionContext
  implicit val system: ActorSystem = ActorSystem("PortfolioSystem")
  implicit val materializer: Materializer = Materializer(system)
  implicit val ec: ExecutionContext = system.dispatcher

//  //API key for Alpha Vantage
//  val apiKey = "6L4F5FO8C8QOMQRN" // Replace with your Alpha Vantage API key
//
//  // Instantiate AlphaVantageClient
//  val alphaVantageClient = new AlphaVantageClient(apiKey)
//
//  // Instantiate Portfolio and pass the AlphaVantageClient instance as a dependency
//  val portfolio = Portfolio(alphaVantageClient)
//    .buy(Stock("AAPL"), Quantity(100))
//    .sell(StockOption("AAPL", "CALL", "2024-12-31", 150), Quantity(10))
//    .buy(Stock("GOOGL"), Quantity(50), Some(LimitPrice(2800)))
//    .marketBuy(Stock("GOOGL"), Quantity(50))
//    .marketSell(Stock("AMZN"), Quantity(20))
//
//  //two kinds of method to output order
//  println(portfolio.getOrders)
//  portfolio.show()
//  println()
//
//  // Simulate market order executions with real-time price fetching
//  portfolio.executeMarketOrders()  // Execute market orders
//  println()
//
//
//  // The stock symbol for which we want to fetch the real-time price
//  val symbol = "AAPL"  // Replace with my desired stock symbol
//
//  // Fetch real-time price using the AlphaVantageClient
//  alphaVantageClient.getRealTimePrice(symbol).onComplete {
//    case Success(priceOpt) =>
//      println(s"The latest price for $symbol is: ${priceOpt.getOrElse("Unavailable")}")
//    case Failure(exception) =>
//      println(s"Failed to fetch price: $exception")
//  }
//
//  // Example of interacting with the portfolio, you can add buy/sell orders
//  portfolio.buy("AAPL", Quantity(10)) // Adding a buy order for AAPL
//
//  // Show the portfolio orders
//  portfolio.show()
//  println()


/*
  API key for YahooFinanceClient
 */
//  //API key for YahooFinanceClient
//  // Instantiate YahooFinanceClient
//  val yahooFinanceClient = new YahooFinanceClient()
//
//  // Example: Fetch real-time price for AAPL
//  val symbol = "AAPL"
//
//  yahooFinanceClient.getRealTimePrice(symbol).onComplete {
//    case Success(Some(price)) =>
//      println(s"The current price of $symbol is $$ $price")
//    case Success(None) =>
//      println(s"Could not fetch the price for $symbol.")
//    case Failure(exception) =>
//      println(s"Failed to fetch price: $exception")
//  }

  /*
  API key for finnhubClient
 */

  val apiKey = "ct391jpr01qkff714tggct391jpr01qkff714th0" // Replace with my Finnhub API key
  val finnhubClient = new FinnhubClient(apiKey)

  val symbol = "AAPL"

  finnhubClient.getRealTimePrice(symbol).onComplete {
    case Success(Some(price)) =>
      println(s"The current price of $symbol is $$ $price")
    case Success(None) =>
      println(s"Could not fetch the price for $symbol.")
    case Failure(exception) =>
      println(s"Failed to fetch price: $exception")
  }


  // Fetch historical prices --Due to authority issue，I can't get history data
  // {"error":"You don't have access to this resource."}
  val resolution = "D" // Example: 1-day resolution
  val to: Long = Instant.now.getEpochSecond                  // Current time in seconds (Unix timestamp)
  val from: Long = Instant.now.minus(10, ChronoUnit.DAYS).getEpochSecond // 30 days ago

  def formatTimestamp(timestamp: Long): String = {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    Instant.ofEpochSecond(timestamp).atZone(ZoneId.systemDefault()).format(formatter)
  }

  finnhubClient.getHistoricalPrices(symbol, resolution, from, to).onComplete {
    case Success(Some(candles)) =>
      println(s"Historical prices for $symbol:")
      candles.t.zip(candles.c).foreach { case (timestamp, closePrice) =>
        println(s"Date: ${formatTimestamp(timestamp)}, Close Price: $closePrice")
      }
    case Success(None) =>
      println(s"Could not fetch historical prices for $symbol.")
    case Failure(exception) =>
      println(s"Failed to fetch historical prices: $exception")
  }

  // Shutdown the ActorSystem after a delay
  system.scheduler.scheduleOnce(scala.concurrent.duration.Duration(10, "seconds")) {
    system.terminate()
  }
}
