package dsl

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers


class PortfolioSpec extends AnyFunSuite with Matchers {

  test("Add an order to the portfolio") {
    val portfolio = Portfolio()
    val order = Order(Buy, Quantity(10), Stock("AAPL"))

    portfolio.addOrder(order)

    portfolio.getOrders should contain(order)
  }

  test("Remove an order from the portfolio") {
    val portfolio = Portfolio()
    val order = Order(Buy, Quantity(10), Stock("AAPL"))

    portfolio.addOrder(order)
    portfolio.removeOrder(order)

    portfolio.getOrders shouldBe empty
  }

  test("Retrieve all orders from the portfolio") {
    val portfolio = Portfolio()

    val order1 = Order(Buy, Quantity(10), Stock("AAPL"))
    val order2 = Order(Sell, Quantity(5), Stock("GOOD"))

    portfolio.addOrder(order1)
    portfolio.addOrder(order2)

    portfolio.getOrders should contain theSameElementsAs List(order1, order2)
  }

  test("Place a buy order using the DSL") {
    val portfolio = Portfolio()
    val orderBuilder = portfolio.buy(10)
    val order = Order(orderBuilder.action, orderBuilder.quantity, Stock("AAPL"))

    portfolio.addOrder(order)

    portfolio.getOrders should contain(order)
  }

  test("Place a sell order using the DSL") {
    val portfolio = Portfolio()
    val orderBuilder = portfolio.sell(5)
    val order = Order(orderBuilder.action, orderBuilder.quantity, Stock("GOOD"))

    portfolio.addOrder(order)

    portfolio.getOrders should contain(order)
  }

  test("Add a DSL-built order to the portfolio") {
    val portfolio = Portfolio()
    val assetBuilder = Trade.AssetBuilder(Buy, Quantity(10), Stock("TSLA"))

    portfolio.addDslOrder(assetBuilder, Market)

    portfolio.getOrders should contain(Order(Buy, Quantity(10), Stock("TSLA"), Some(Market)))
  }

  test("Print portfolio summary") {
    val portfolio = Portfolio()
    val order1 = Order(Buy, Quantity(10), Stock("AAPL"))
    val order2 = Order(Sell, Quantity(5), Stock("GOOD"))

    portfolio.addOrder(order1)
    portfolio.addOrder(order2)

    // This will print to the console, verify manually
    portfolio.summary()
  }

  test("Display portfolio details") {
    val portfolio = Portfolio()
    val order = Order(Buy, Quantity(10), Stock("AAPL"), Some(Market))

    portfolio.addOrder(order)

    // This will print detailed info, verify manually
    portfolio.show()
  }
}
