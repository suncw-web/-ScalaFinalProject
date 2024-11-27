import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import io.circe.generic.auto._
import io.circe.parser._

import scala.concurrent.{ExecutionContext, Future}

case class QuoteResult(symbol: String, regularMarketPrice: Double)
case class QuoteResponse(result: List[QuoteResult], error: Option[String])
case class YahooFinanceResponse(quoteResponse: QuoteResponse)

class YahooFinanceClient()(implicit system: ActorSystem, materializer: Materializer, ec: ExecutionContext) {

  private val baseUrl = "https://query1.finance.yahoo.com/v7/finance/quote"

  /** Fetch real-time stock price for a given symbol */
  def getRealTimePrice(symbol: String): Future[Option[Double]] = {
    val url = s"$baseUrl?symbols=$symbol"

    val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = url))

    responseFuture.flatMap { response =>
      Unmarshal(response.entity).to[String].map { jsonString =>
        println(s"Raw JSON response: $jsonString") // Debug: Print the raw JSON

        decode[YahooFinanceResponse](jsonString) match {
          case Right(parsedResponse) =>
            // Extract the price from the response
            parsedResponse.quoteResponse.result.headOption.map(_.regularMarketPrice)

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
