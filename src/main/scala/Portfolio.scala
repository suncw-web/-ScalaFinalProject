import scala.concurrent.ExecutionContext.Implicits.global


//Create a Portfolio Class
// This will represent the user's portfolio, which can contain multiple orders.
// The Portfolio class will have methods for adding BuyOrder and SellOrder
class Portfolio (alphaVantageClient: AlphaVantageClient){
  private var orders: List[Order] = List()

  // Method to place a buy order
  def buy(asset: Any, quantity: Quantity, limitPrice: Option[LimitPrice] = None): Portfolio = {
    orders = BuyOrder(asset, quantity, limitPrice) :: orders
    this
  }

  // Method to place a sell order
  def sell(asset: Any, quantity: Quantity): Portfolio = {
    orders = SellOrder(asset, quantity) :: orders
    this
  }

  // Method to place a market buy order (at current market price)
  def marketBuy(asset: Any, quantity: Quantity): Portfolio = {
    orders = MarketOrder(asset, quantity) :: orders
    this
  }

  // Method to place a market sell order (at current market price)
  def marketSell(asset: Any, quantity: Quantity): Portfolio = {
    orders = MarketOrder(asset, quantity) :: orders
    this
  }

  // Method to simulate the execution of market orders
  def executeMarketOrders(): Unit = {
    orders.foreach {
      case MarketOrder(asset, quantity) =>
        // Fetch the real-time price from Alpha Vantage API
        asset match {
          case Stock(symbol) =>
            alphaVantageClient.getRealTimePrice(symbol).foreach {
              case Some(price) =>
                println(s"Executing market order: $quantity of $asset at $price per unit")
              case None =>
                println(s"Failed to fetch price for $asset. Order cannot be executed.")
            }
          case StockOption(symbol, optionType, expiry, strike) =>
            println(s"Market orders for options are not yet supported in this mock API.")
          case _ =>
            println(s"Unsupported asset type for market order: $asset")
        }
      case _ =>// For other types of orders, no execution simulation
    }
  }

  // Method to display the portfolio orders in list
  def getOrders: List[Order] = orders

  // Method to display the portfolio orders by order
  def show(): Unit = {
    orders.foreach {
      case BuyOrder(asset, quantity, Some(limit)) =>
        println(s"Buy $quantity of $asset with limit price $limit")
      case BuyOrder(asset, quantity, None) =>
        println(s"Buy $quantity of $asset")
      case SellOrder(asset, quantity) =>
        println(s"Sell $quantity of $asset")
      case MarketOrder(asset, quantity) =>
        println(s"Market order for $quantity of $asset at current market price")
    }
  }
}

object Portfolio {
  def apply(alphaVantageClient: AlphaVantageClient): Portfolio = new Portfolio(alphaVantageClient)
}