package apiClient

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import io.circe.generic.auto._
import io.circe.parser._

import scala.concurrent.{ExecutionContext, Future}

case class FinnhubQuote(
                         c: Double, // Current price
                         d: Double, // Change in price
                         dp: Double, // Percentage change
                         h: Double, // High price
                         l: Double, // Low price
                         o: Double, // Opening price
                         pc: Double // Previous close price
                       ) // "c" is the current price

// To fetch historical prices using Finnhub, I use their Stock Candles API
// Define the case class FinnhubClient to match the API response:
case class FinnhubCandles(c: Seq[Double], h: Seq[Double], l: Seq[Double], o: Seq[Double], s: String, t: Seq[Long], v: Seq[Double])


class FinnhubClient(apiKey: String)(implicit system: ActorSystem, materializer: Materializer, ec: ExecutionContext) {
  private val baseUrl = "https://finnhub.io/api/v1/quote"

  /** Fetch real-time stock price for a given symbol */
  def getRealTimePrice(symbol: String): Future[Option[Double]] = {
    val url = s"$baseUrl?symbol=$symbol&token=$apiKey"

    Http().singleRequest(HttpRequest(uri = url)).flatMap { response =>
      Unmarshal(response.entity).to[String].map { jsonString =>
        println(s"Raw JSON response: $jsonString") // Debug: Print the raw JSON
        decode[FinnhubQuote](jsonString) match {
          case Right(quote) => Some(quote.c)
          case Left(error) =>
            println(s"Error parsing JSON response: $error")
            None
        }
      }
    }.recover {
      case ex: Exception =>
        println(s"Error fetching real-time price: $ex")
        None
    }
  }

  /** Fetch Historical stock price for a given symbol */
def getHistoricalPrices(symbol: String, resolution: String, from: Long, to: Long)
                       (implicit system: ActorSystem, materializer: Materializer, ec: ExecutionContext): Future[Option[FinnhubCandles]] = {
  val url = s"https://finnhub.io/api/v1/stock/candle?symbol=$symbol&resolution=$resolution&from=$from&to=$to&token=$apiKey"

  Http().singleRequest(HttpRequest(uri = url)).flatMap { response =>
    Unmarshal(response.entity).to[String].map { jsonString =>
      println(s"Raw JSON response: $jsonString") // Debugging
      decode[FinnhubCandles](jsonString) match {
        case Right(candles) if candles.s == "ok" => Some(candles)
        case Right(_) =>
          println("API returned an error or no data.")
          None
        case Left(error) =>
          println(s"Error parsing JSON response: $error")
          None
      }
    }
  }.recover {
    case ex: Exception =>
      println(s"Error fetching historical prices: $ex")
      None
  }
  }
}
