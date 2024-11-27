//case class DomainConcepts()


// Define the Core Domain Concepts of DSL (Domain Specific language)
// First, we define the main entities in our domain, such as Stock, StockOption, Quantity, and LimitPrice.
// These will be used to specify the buy and sell orders.


// Representing stocks and stockOptions
case class Stock(symbol: String)
// define stockOption action, it's different with scala Option
// Represents an option, with parameters like symbol, optionType (e.g., "CALL" or "PUT"),
// CALL Option means you are betting that the stock price will go up
// PUT Option means you are betting that the stock price will go down
// expiry date, and strike price.
case class StockOption(symbol: String, optionType: String, expiry: String, strike: Double)

// Representing quantity and price
case class Quantity(amount: Int)
case class LimitPrice(price: Double)

// Orders for trading
// Scala's Option is a fundamental type
// Define Buy and Sell Order Types
// Create case class
sealed trait Order
// Stores details about a buy order, including the asset (stock/stockOption), quantity, and an optional limit price.
case class BuyOrder(asset: Any, quantity: Quantity, limitPrice: Option[LimitPrice] = None) extends Order
//Stores details about a sell order, including the asset and quantity.
case class SellOrder(asset: Any, quantity: Quantity) extends Order

// Define the new class for market orders
case class MarketOrder(asset: Any, quantity: Quantity) extends Order