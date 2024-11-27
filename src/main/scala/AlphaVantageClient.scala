//import sttp.client3._
//import sttp.client3.circe._
//import sttp.client3.impl.cats._
//import sttp.client3.asynchttpclient.cats._
//
//
//import io.circe.generic.auto._
//import io.circe.syntax._
//import io.circe.parser._
//import cats.effect._
//import scala.concurrent.Future
//import scala.concurrent.ExecutionContext.Implicits.global
//import sttp.client3.impl.cats.AsyncHttpClientCatsBackend  // Import for backend
//
//// Case classes for the Alpha Vantage response
//case class TimeSeriesData(open: String, high: String, low: String, close: String, volume: String)
//case class AlphaVantageResponse(`Time Series (1min)`: Map[String, TimeSeriesData])
//
//// Alpha Vantage client for real-time price fetching
//object AlphaVantageClient {
//
//  private val apiKey = "your-api-key"  // Replace with your actual API key
//  private val baseUrl = "https://www.alphavantage.co/query"
//
//  // Function to get real-time price for a stock symbol
//  def getRealTimePrice(symbol: String): Future[Option[Double]] = {
//    //  Make an HTTP GET request using STTP
//    val request = basicRequest.get(uri"$baseUrl?function=TIME_SERIES_INTRADAY&symbol=$symbol&interval=1min&apikey=$apiKey")
//      .response(asJson[AlphaVantageResponse]) // Expect response as JSON into AlphaVantageResponse case class
//
//    // Send the request and process the response
//    request.send(backend).flatMap { response =>
//      // The response is a Future[Response[Either[String, AlphaVantageResponse]]]
//      response.body match {
//        case Right(result) =>
//          val latestTime = result.`Time Series (1min)`.keys.headOption
//          latestTime.flatMap { time =>
//            val data = result.`Time Series (1min)`(time)
//            Some(data.close.toDouble)  // Extract close price
//          } match {
//            case Some(price) => Future.successful(Some(price))
//            case None => Future.successful(None)
//          }
//
//        case Left(error) =>
//          println(s"Error fetching data: $error")  // Handle the error
//          Future.successful(None)  // Return None if there's an error
//      }
//    }
//  }
//
//  // Initialize STTP backend (use Cats-Effect backend)
//  // Initialize STTP backend (use Cats-Effect backend)
//  val backend: SttpBackend[Future, Any] = AsyncHttpClientCatsBackend()
//}

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