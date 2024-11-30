package MockExchange

import dsl.{Stock, StockOption}
// Mock Exchange simulating real-time price retrieval

object MockExchange {
  // Simulating current market prices for some assets
  private val marketPrices: Map[String, Double] = Map(
    "AAPL" -> 145.32, // Price for AAPL stock
    "GOOGL" -> 2740.56, // Price for GOOGL stock
    "AMZN" -> 3341.21, // Price for AMZN stock
    "AAPL_CALL_150" -> 5.25, // Price for AAPL Call Option at strike 150
    "GOOGL_CALL_2800" -> 100.75 // Price for GOOGL Call Option at strike 2800
  )

  // Function to get the current market price for an asset (e.g., stock or stockOption)
  def getPrice(asset: Any): Option[Double] = asset match {
    case Stock(symbol) => marketPrices.get(symbol)
    case StockOption(symbol, optionType, expiry, strike) =>
      marketPrices.get(s"${symbol}_${optionType}_$strike")
    case _ => None // In case the asset type isn't supported
  }
}
