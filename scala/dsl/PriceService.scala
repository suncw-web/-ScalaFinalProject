package dsl

import scala.concurrent.{ExecutionContext, Future}
import scala.collection.mutable

object PriceService {
   private val cache: mutable.Map[String, Double] = mutable.Map()

  def getCache: Map[String, Double] = cache.toMap // Read-only access for testing

  // Fetch one price
  def fetchPrice(symbol: String, fetchPriceFn: String => Future[Double])
                (implicit ec: ExecutionContext): Future[Double] = {
    cache.get(symbol) match {
      case Some(price) => Future.successful(price) // Return cached price
      case None =>
        fetchPriceFn(symbol).map { price =>
          cache.update(symbol, price) // Update cache
          price
        }
    }
  }

  // Fetch many prices. A batch fetching method to reduce the number of API calls.
  def fetchPrices(symbols: List[String], batchFetchFn: List[String] => Future[Map[String, Double]])
                 (implicit ec: ExecutionContext): Future[Map[String, Double]] = {
    batchFetchFn(symbols)
  }

  // Adding a caching mechanism will reduce redundant API calls.
  def clearCache(): Unit = {
    cache.clear()
  }

  // A method that gracefully handles errors and logs them for debugging purposes.
  def fetchPriceWithErrorHandling(symbol: String, fetchPriceFn: String => Future[Double])
                                 (implicit ec: ExecutionContext): Future[Option[Double]] = {
    fetchPriceFn(symbol).map(Some(_)).recover {
      case ex: Exception =>
        println(s"Failed to fetch price for $symbol: ${ex.getMessage}")
        None
    }
  }

//  // A method to fetch historical prices (e.g., for analysis)
//  def fetchHistoricalPrices(symbol: String, fetchHistoricalFn: String => Future[Map[String, Double]])
//                           (implicit ec: ExecutionContext): Future[Map[String, Double]] = {
//    fetchHistoricalFn(symbol)
//  }
//
//  def calculateAveragePrice(prices: List[Double]): Double = {
//    if (prices.nonEmpty) prices.sum / prices.size else 0.0
//  }
//
//  def calculatePriceRange(prices: List[Double]): (Double, Double) = {
//    if (prices.nonEmpty) (prices.min, prices.max) else (0.0, 0.0)
//  }
//
//  //Add a timeout mechanism to prevent application from hanging.
//  def fetchPriceWithTimeout(symbol: String, fetchPriceFn: String => Future[Double], timeout: FiniteDuration)
//                           (implicit ec: ExecutionContext, system: akka.actor.ActorSystem): Future[Double] = {
//    val timeoutFuture = after(timeout, system.scheduler)(Future.failed(new TimeoutException("Request timed out")))
//    Future.firstCompletedOf(Seq(fetchPriceFn(symbol), timeoutFuture))
//  }
//
//  // A method that streams prices (if the API supports it) using Akka Streams.
//  def streamPrices(symbol: String, streamFetchFn: String => Source[Double, _]): Source[Double, _] = {
//    streamFetchFn(symbol)
//  }
}