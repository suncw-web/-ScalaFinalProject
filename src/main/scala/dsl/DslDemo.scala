package dsl

import Trade._


object DslDemo extends App {

  // Sample orders, no use portfolio to manage orders
  private val stockOrder = buy(100).of("AAPL").atLimit(150.0)
  private val optionOrder = sell(50).ofOption("GOOGL", Call, 2500.0, "2024-12-31").atMarket()
  private val marketOrder = sell(50).of("AAPL").atStop(280.0)

  println(stockOrder)  // Output: Order(BUY,100,Stock(AAPL),Limit(150.0))
  println(optionOrder) // Output: Order(Sell,Quantity(50),StockOption(GOOGL,Call,Some(2500.0),Some(2024-12-31)),Some(Market),GTC,Pending)
  println(marketOrder) // Output: Order(SELL,50, Stock(AAPL), stop(280.0))
  println()

  // Create a portfolio instance
  val portfolio = Portfolio()

  // Place buy orders using the DSL
  private val buyOrder1 = portfolio.buy(100).of("AAPL").atMarket()  // Market order for 100 AAPL shares
  private val buyOrder2 = portfolio.buy(50).of("GOOG").atLimit(1500.0)  // Limit order for 50 GOOG shares at $1500

  // Place sell orders using the DSL
  private val sellOrder1 = portfolio.sell(100).of("AAPL").atStop(140.0)  // Stop order for 100 AAPL shares at $140
  private val sellOrder2 = portfolio.sell(300).ofOption("MMM", Call, 132.0, "2024-12-31").atMarket() //Order

  // Add orders to the portfolio
  portfolio.addOrder(buyOrder1)
  portfolio.addOrder(buyOrder2)
  portfolio.addOrder(sellOrder1)
  portfolio.addOrder(sellOrder2)

  // Show portfolio summary
  portfolio.summary()  // Prints the summary of all orders in the portfolio
  println()

  portfolio.removeOrder(buyOrder1)

  //output order list
  println(portfolio.getOrders)

  // Show the portfolio details again after execution
  portfolio.show()
  println()

  // Demo adding a DSL-built order directly to the portfolio
  val assetBuilder = portfolio.buy(10).ofOption("AAPL", Call, 150.0, "2024-12-15")
  portfolio.addDslOrder(assetBuilder, Market)

  // addTo is a method of AssetBuilder
  private val assetBuilder1 = portfolio.buy(20).ofOption("MMM", Call, 132.0, "2024-12-31")
  assetBuilder1.addTo(portfolio, Limit(145))

  // withAttributes is another method of AssetBuilder
  private val assetBuilder2 = assetBuilder1.withAttributes(Market, IOC)
  portfolio.addOrder(assetBuilder2)

  // Show updated portfolio after adding option order
  portfolio.show()

}