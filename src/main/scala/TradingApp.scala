import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.http.scaladsl.Http

import scala.concurrent.ExecutionContext

import scala.util.{Failure, Success}

import java.time.Instant
import java.time.temporal.ChronoUnit
import java.time.format.DateTimeFormatter
import java.time.ZoneId

import apiClient.FinnhubClient

object TradingApp extends App{
  println("Hello, TradingApp!")
  println("Enter the stock symbol you want to check (e.g., AAPL):")

  // Read the stock symbol from the user
  val symbol = scala.io.StdIn.readLine().toUpperCase()
  //val symbol = "AAPL"


  // User Methods to Create Buy/Sell Orders
  // Creates a Portfolio and adds several orders (buy and sell).
  // It shows the portfolio's orders with the getOrder or show method.

  // Define implicit ActorSystem, Materializer, and ExecutionContext
  implicit val system: ActorSystem = ActorSystem("PortfolioSystem")
  implicit val materializer: Materializer = Materializer(system)
  implicit val ec: ExecutionContext = system.dispatcher

  /*
  API key for finnhubClient, can get real time price.
 */

  val apiKey = "ct391jpr01qkff714tggct391jpr01qkff714th0" // Replace with my Finnhub API key
  val finnhubClient = new FinnhubClient(apiKey)



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

  private def formatTimestamp(timestamp: Long): String = {
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
  system.scheduler.scheduleOnce(scala.concurrent.duration.Duration(5, "seconds")) {
    system.terminate()
  }
}
