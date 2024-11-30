package MockExchange

import dsl.{Stock, StockOption}

object MockExchangeDemo extends App {
  // Fetching the price for a stock
  private val appleStock = Stock("AAPL")
  private val applePrice = MockExchange.getPrice(appleStock)
  println(s"Current market price for ${appleStock.symbol}: ${applePrice.getOrElse("Unavailable")}")

  // Fetching the price for a stock option
  private val googleCallOption = StockOption("GOOGL", "CALL", "2024-12-31", 2800)
  private val googleOptionPrice = MockExchange.getPrice(googleCallOption)
  println(s"Current market price for Google Call Option: ${googleOptionPrice.getOrElse("Unavailable")}")

  // Fetching the price for an unsupported asset
  private val unsupportedAsset = "UnsupportedAsset"
  private val unsupportedPrice = MockExchange.getPrice(unsupportedAsset)
  println(s"Market price for unsupported asset: ${unsupportedPrice.getOrElse("Unavailable")}")
}

