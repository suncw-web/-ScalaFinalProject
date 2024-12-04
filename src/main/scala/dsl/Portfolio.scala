package dsl

import dsl.Trade.{AssetBuilder, OrderBuilder}


// Create a Portfolio Class
// Encapsulate operations in a fluent interface
// Single Responsibility Principle (SRP)
// This will represent the user's portfolio, which can contain multiple orders.
// The Portfolio class will have methods for adding or removing order
class Portfolio {
  private var orders: List[Order] = List()

  // Add an order to the portfolio
  def addOrder(order: Order): Unit = {
    orders = orders :+ order
  }

  // Remove an order from the portfolio
  def removeOrder(order: Order): Unit = {
    orders = orders.filterNot(_ == order)
  }

  // Retrieve all orders
  def getOrders: List[Order] = orders

  // Print a summary of the portfolio
  def summary(): Unit = {
    println("Portfolio Summary:")
    orders.foreach(println)
  }

  // Place a buy order using the DSL
  def buy(quantity: Int): OrderBuilder = Trade.buy(quantity)


  // Place a sell order using the DSL
  def sell(quantity: Int): OrderBuilder = Trade.sell(quantity)

  // Add a DSL-built order directly to the portfolio
  def addDslOrder(assetBuilder: AssetBuilder, orderType: OrderType): Unit = {
    assetBuilder match {
      case builder: Trade.AssetBuilder =>
        val order = Order(builder.action, builder.quantity, builder.asset, Some(orderType))
        addOrder(order)
      case _ =>
         println("Invalid order builder type")
    }
  }

  // Display the portfolio with more detail
  def show(): Unit = {
    println("Portfolio Details:")
    orders.foreach {
      case Order(action, Quantity(amount), asset, Some(orderType), _, _) =>
        println(s"$action $amount of $asset as $orderType")
      case Order(action, Quantity(amount), asset, None, _, _) =>
        println(s"$action $amount of $asset with no specific order type")
    }
  }
}

object Portfolio {
  def apply(): Portfolio = new Portfolio()
}