package dsl

//case class DomainConcepts()


// First, we define the main entities in our domain, such as Stock, Options(StockOption), Quantity, and LimitPrice.
// These will be used to specify the buy and sell orders.

/*
Define the Core Domain Concepts of DSL (Domain Specific language)
First, we define the main entities in our domain, as below:
Actions: Buy or Sell.
Assets: Stocks and options(StockOption).
Attributes: Quantity, price, type of order, expiration date (for options).
Order Types: Market, Limit, Stop, etc.
 */

sealed trait Asset {
  def symbol: String
}
/**
 * Representing stocks
 * @param symbol means stock symbol
 */
case class Stock(symbol: String) extends Asset

// Represents an option, with parameters like symbol, optionType (e.g., "CALL" or "PUT"),
// CALL Option means you are betting that the stock price will go up
// PUT Option means you are betting that the stock price will go down
// expiry date, and strike price.

/**
 * define stockOption, it's different with scala Option
 * @param symbol
 * @param optionType e.g., "CALL" or "PUT".
 *                   CALL Option means you are betting that the stock price will go up
 *                   PUT Option means you are betting that the stock price will go down
 * @param expiry expiry date, is Option, explicitly handle the absence of values without risking NullPointerException
 * @param strike strike price, is Option too.
 */
case class StockOption(symbol: String, optionType: OptionType, strike: Option[Double], expiry: Option[String]) extends Asset

sealed trait Action
case object Buy extends Action
case object Sell extends Action

sealed trait OptionType
case object Call extends OptionType
case object Put extends OptionType


// Representing quantity
case class Quantity(amount: Int) {
  require(amount > 0, "Quantity must be positive")
}

sealed trait OrderType
case object Market extends OrderType
case class Limit(price: Double) extends OrderType
case class Stop(price: Double) extends OrderType


sealed trait TimeInForce
case object GTC extends TimeInForce
case object IOC extends TimeInForce

/**
 * Create an Order
 * @param action
 * @param quantity
 * @param asset
 * @param orderType
 * @param timeInForce
 * @param status
 */
case class Order(
                  action: Action,
                  quantity: Quantity,
                  asset: Asset,
                  orderType: Option[OrderType] = Some(Market),
                  timeInForce: TimeInForce = GTC,
                  status: String = "Pending"
                )
