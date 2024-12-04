package dsl

// A DSL Builder
object Trade {
  def buy(quantity: Int): OrderBuilder = OrderBuilder(Buy, Quantity(quantity))
  def sell(quantity: Int): OrderBuilder = OrderBuilder(Sell, Quantity(quantity))

  case class OrderBuilder(action: Action, quantity: Quantity) {
    /**
     * Handle stock.
     * @param stockSymbol Stock symbol
     * @return AssetBuilder
     */
    def of(stockSymbol: String): AssetBuilder = AssetBuilder(action, quantity, Stock(stockSymbol))

    /**
     * Handle stock option.
     * @param symbol Stock symbol
     * @param optionType Option type (Call/Put)
     * @param strike Strike price
     * @param expiry Expiry date
     * @return AssetBuilder
     */
    def ofOption(symbol: String, optionType: OptionType, strike: Double, expiry: String): AssetBuilder = {
      require(strike > 0, "Strike price must be positive")
      AssetBuilder(action, quantity, StockOption(symbol, optionType, Some(strike), Some(expiry)))
    }
  }

  case class AssetBuilder(action: Action, quantity: Quantity, asset: Asset) {
    /**
     * Create a market order.
     * @return Order
     */
    def atMarket(): Order = Order(action, quantity, asset, Some(Market))

    /**
     * Create a limit order.
     * @param price Limit price
     * @return Order
     */
    def atLimit(price: Double): Order = {
      require(price > 0, "Price must be positive")
      Order(action, quantity, asset, Some(Limit(price)))
    }

    /**
     * Create a stop order.
     * @param price Stop price
     * @return Order
     */
    def atStop(price: Double): Order = {
      require(price > 0, "Price must be positive")
      Order(action, quantity, asset, Some(Stop(price)))
    }

    /**
     * Add the order to a portfolio.
     * @param portfolio Portfolio instance
     * @param orderType Order type
     */
    def addTo(portfolio: Portfolio, orderType: OrderType): Unit = {
      val order = Order(action, quantity, asset, Some(orderType))
      portfolio.addOrder(order)
    }

    /**
     * Create an order with additional attributes like TimeInForce.
     * @param orderType Order type
     * @param timeInForce Time in Force
     * @return Order
     */
    def withAttributes(orderType: OrderType, timeInForce: TimeInForce = GTC): Order = {
      Order(action, quantity, asset, Some(orderType))
    }
  }
}
