package dsl

// 增加校验功能
sealed trait TimeInForce
case object GTC extends TimeInForce // Good Till Cancelled
case object IOC extends TimeInForce // Immediate Or Cancel
case object FOK extends TimeInForce // Fill Or Kill

sealed trait TriggerCondition
case object NoneCondition extends TriggerCondition
case class PriceAbove(value: Double) extends TriggerCondition
case class PriceBelow(value: Double) extends TriggerCondition

// A DSL Builder with validation
object Trade {
  def buy(quantity: Int): OrderBuilder = OrderBuilder(Buy, validateQuantity(quantity))
  def sell(quantity: Int): OrderBuilder = OrderBuilder(Sell, validateQuantity(quantity))

  // 校验数量
  private def validateQuantity(quantity: Int): Quantity = {
    require(quantity > 0, "Quantity must be positive")
    Quantity(quantity)
  }

  case class OrderBuilder(action: Action, quantity: Quantity) {
    def of(stockSymbol: String): AssetBuilder = AssetBuilder(action, quantity, validateStock(stockSymbol))
    def ofOption(symbol: String, optionType: OptionType, strike: Double, expiry: String): AssetBuilder = {
      require(strike > 0, "Strike price must be positive")
      require(expiry.matches("\\d{4}-\\d{2}-\\d{2}"), "Expiry date must be in format YYYY-MM-DD")
      AssetBuilder(action, quantity, StockOption(symbol, optionType, Some(strike), Some(expiry)))
    }

    private def validateStock(symbol: String): Stock = {
      require(symbol.nonEmpty, "Stock symbol must not be empty")
      Stock(symbol)
    }
  }

  case class AssetBuilder(
                           action: Action,
                           quantity: Quantity,
                           asset: Asset,
                           timeInForce: TimeInForce = GTC,
                           trigger: TriggerCondition = NoneCondition
                         ) {
    def atLimit(price: Double): Order = {
      validatePrice(price)
      Order(action, quantity, asset, Some(Limit(price)), timeInForce, trigger)
    }

    def atMarket(): Order = Order(action, quantity, asset, Some(Market), timeInForce, trigger)

    def atStop(price: Double): Order = {
      validatePrice(price)
      Order(action, quantity, asset, Some(Stop(price)), timeInForce, trigger)
    }

    def withTimeInForce(tif: TimeInForce): AssetBuilder = this.copy(timeInForce = tif)

    def withTrigger(condition: TriggerCondition): AssetBuilder = {
      validateTrigger(condition)
      this.copy(trigger = condition)
    }

    private def validatePrice(price: Double): Unit = {
      require(price > 0, "Price must be positive")
    }

    private def validateTrigger(condition: TriggerCondition): Unit = condition match {
      case PriceAbove(value) =>
        require(value > 0, "Trigger price (PriceAbove) must be positive")
      case PriceBelow(value) =>
        require(value > 0, "Trigger price (PriceBelow) must be positive")
      case NoneCondition => // No validation needed
    }
  }
}
