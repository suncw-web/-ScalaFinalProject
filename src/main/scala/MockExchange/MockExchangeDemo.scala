package MockExchange

import dsl.{Call, Put, Stock, StockOption}

object MockExchangeDemo extends App {
  // Fetching the price for a stock
  private val appleStock = Stock("AAPL")
  private val applePrice = MockExchange.getPrice(appleStock)
  println(s"Current market price for ${appleStock.symbol}: ${applePrice.getOrElse("Unavailable")}")

  // Fetching the price for a stock option
  private val googleCallOption = StockOption("GOOGL", Call, Some(2800), Some("2024-12-31"))
  private val googleOptionPrice = MockExchange.getPrice(googleCallOption)
  println(s"Current market price for ${googleCallOption.symbol} ${googleCallOption.optionType} Option: ${googleOptionPrice.getOrElse("Unavailable")}")

  // Fetching the price for a stock option
  private val applePutOption = StockOption("AAPL", Put, Some(250), Some("2024-12-31"))
  private val appleOptionPrice = MockExchange.getPrice(applePutOption)
  println(s"Current market price for ${applePutOption.symbol} ${applePutOption.optionType} Option " +
    s"at strike ${applePutOption.strike}: ${appleOptionPrice.getOrElse("Unavailable")}")

  // Fetching the price for an unsupported asset
  private val unsupportedAsset = "UnsupportedAsset"
  private val unsupportedPrice = MockExchange.getPrice(unsupportedAsset)
  println(s"Market price for unsupported asset: ${unsupportedPrice.getOrElse("Unavailable")}")
}

