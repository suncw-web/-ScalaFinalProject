package MockExchange

import dsl.{Stock, StockOption}
// Mock Exchange simulating real-time price retrieval

object MockExchange {
  // Simulating current market prices for some assets
  private val marketPrices: Map[String, Double] = Map(
    "AAPL" -> 240.91, // Price for AAPL stock
    "GOOGL" -> 2740.56, // Price for GOOGL stock
    "AMZN" -> 3341.21, // Price for AMZN stock
    "AAPL_PUT_250.0" -> 5.25, // Price for AAPL Put Option at strike 250
    "GOOGL_CALL_2800.0" -> 100.75 // Price for GOOGL Call Option at strike 2800
  )

  def getPrice(asset: Any): Option[Double] = asset match {
    case Stock(symbol) =>
      marketPrices.get(symbol)
    case StockOption(symbol, optionType, Some(strike), _) =>
      marketPrices.get(s"${symbol}_${optionType.toString.toUpperCase}_$strike")
    case StockOption(_, _, None, _) =>
      println("StockOption missing strike price.")
      None
    case _ =>
      println(s"Unsupported asset type: ${asset.getClass.getSimpleName}")
      None
  }
}