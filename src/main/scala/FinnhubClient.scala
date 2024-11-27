import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import io.circe.generic.auto._
import io.circe.parser._
import akka.stream.Materializer

import scala.concurrent.{ExecutionContext, Future}

case class FinnhubQuote(c: Double) // "c" is the current price

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
}
