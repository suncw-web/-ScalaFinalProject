//akka method
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import io.circe.generic.auto._
import io.circe.parser._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}
// Case classes for Alpha Vantage API response
case class TimeSeriesData(open: String, high: String, low: String, close: String, volume: String)
case class AlphaVantageError(Information: String)
case class AlphaVantageResponse(`Time Series (1min)`: Option[Map[String, TimeSeriesData]])

class AlphaVantageClient(apiKey: String)(implicit system: ActorSystem, materializer: Materializer, ec: ExecutionContext) {
  //private val apiKey = "your-api-key"
  private val baseUrl = "https://www.alphavantage.co/query"
  /** Fetch real-time stock price for a given symbol */
  def getRealTimePrice(symbol: String): Future[Option[Double]] = {
    val url = s"$baseUrl?function=TIME_SERIES_INTRADAY&symbol=$symbol&interval=1min&apikey=$apiKey"
    val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = url))
    responseFuture.flatMap { response =>
      Unmarshal(response.entity).to[String].map { jsonString =>
        println(s"Raw JSON response: $jsonString") // Debug: Print the raw JSON
        // Try decoding as the expected response
        decode[AlphaVantageResponse](jsonString) match {
          case Right(parsedResponse) if parsedResponse.`Time Series (1min)`.isDefined =>
            val timeSeries = parsedResponse.`Time Series (1min)`.get
            timeSeries.keys.headOption.flatMap { latestTime =>
              timeSeries.get(latestTime).map(_.close.toDouble)
            }
          case Right(_) =>
            println("No time series data available in the response.")
            None
          case Left(_) =>
            // If decoding as AlphaVantageResponse fails, try AlphaVantageError
            decode[AlphaVantageError](jsonString) match {
              case Right(errorResponse) =>
                println(s"API returned an informational message: ${errorResponse.Information}")
                None
              case Left(error) =>
                println(s"Error parsing JSON response: $error")
                None
            }
        }
      }
    }.recover {
      case ex: Exception =>
        println(s"Error fetching real-time price: $ex")
        None
    }
  }
}