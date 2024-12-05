package dsl

import akka.actor.ActorSystem

import org.mockito.MockitoSugar
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.{ExecutionContext, Future}

class PriceServiceSpec extends AnyWordSpec with Matchers with ScalaFutures with MockitoSugar {
  implicit val system: ActorSystem = ActorSystem("PriceServiceSpec")
  implicit val ec: ExecutionContext = system.dispatcher

  "PriceService" should {

    "fetch a price and cache it" in {
      val mockFetchPriceFn = mock[String => Future[Double]]
      when(mockFetchPriceFn("AAPL")).thenReturn(Future.successful(150.0))

      val result = PriceService.fetchPrice("AAPL", mockFetchPriceFn)

      whenReady(result) { price =>
        price shouldBe 150.0
      }

      // Verify cache using getCache
      PriceService.getCache should contain("AAPL" -> 150.0)
    }


    "fetch multiple prices using batch fetch" in {
      val mockBatchFetchFn = mock[List[String] => Future[Map[String, Double]]]
      when(mockBatchFetchFn(List("AAPL", "GOOG"))).thenReturn(Future.successful(Map("AAPL" -> 150.0, "GOOG" -> 2800.0)))

      val result = PriceService.fetchPrices(List("AAPL", "GOOG"), mockBatchFetchFn)

      whenReady(result) { prices =>
        prices shouldBe Map("AAPL" -> 150.0, "GOOG" -> 2800.0)
      }
    }

    "handle errors gracefully" in {
      val mockFetchPriceFn = mock[String => Future[Double]]
      when(mockFetchPriceFn("AAPL")).thenReturn(Future.failed(new RuntimeException("Network error")))

      val result = PriceService.fetchPriceWithErrorHandling("AAPL", mockFetchPriceFn)

      whenReady(result) { priceOption =>
        priceOption shouldBe None
      }
    }

    "clear the cache" in {
      // Simulate updating the cache
      PriceService.fetchPrice("AAPL", _ => Future.successful(150.0)).futureValue
      // Verify the cache is updated
      PriceService.getCache should contain("AAPL" -> 150.0)

      // Clear the cache
      PriceService.clearCache()

      // Verify the cache is empty
      PriceService.getCache shouldBe empty
    }

    "cache a fetched price" in {
      val mockFetchPriceFn = mock[String => Future[Double]]
      when(mockFetchPriceFn("AAPL")).thenReturn(Future.successful(150.0))

      val result = PriceService.fetchPrice("AAPL", mockFetchPriceFn)

      whenReady(result) { price =>
        price shouldBe 150.0
        PriceService.getCache should contain ("AAPL" -> 150.0)
      }
    }


  }
}
