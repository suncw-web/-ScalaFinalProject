package dsl

import Trade._

object DslDemo extends App {
  // 单个订单示例
  try {
    val stockOrder = buy(100).of("AAPL").atLimit(150.0).withTimeInForce(GTC)
    val optionOrder = sell(50)
      .ofOption("GOOGL", Call, 2500.0, "2024-12-31")
      .atMarket()
      .withTimeInForce(FOK)
    val stopOrder = sell(50).of("AAPL").atStop(280.0).withTrigger(PriceBelow(270.0))

    println("Valid orders:")
    println(stockOrder)
    println(optionOrder)
    println(stopOrder)
  } catch {
    case ex: IllegalArgumentException => println(s"Error creating order: ${ex.getMessage}")
  }

  // 添加无效订单（验证校验功能）
  try {
    val invalidOrder = buy(-100).of("").atLimit(-150.0) // 不合法的订单
    println(invalidOrder)
  } catch {
    case ex: IllegalArgumentException => println(s"Invalid order caught: ${ex.getMessage}")
  }

  // 使用投资组合示例
  val portfolio = new Portfolio()

  // 添加订单到投资组合
  try {
    val buyOrder1 = portfolio.buy(100).of("MSFT").atMarket().withTimeInForce(IOC)
    portfolio.addOrder(buyOrder1)

    val buyOrder2 = portfolio.buy(50).of("GOOG").atLimit(1500.0)
    portfolio.addOrder(buyOrder2)

    val sellOrder = portfolio.sell(20).of("TSLA").atStop(700.0).withTrigger(PriceBelow(680.0))
    portfolio.addOrder(sellOrder)

    println("\nPortfolio summary:")
    portfolio.summary()
  } catch {
    case ex: IllegalArgumentException => println(s"Error adding order to portfolio: ${ex.getMessage}")
  }

  // 批量订单操作
  try {
    println("\nBatch operations:")
    portfolio.addOrder(buy(30).of("AMZN").atLimit(3300.0))
    portfolio.addOrder(sell(40).of("NVDA").atStop(450.0).withTimeInForce(GTC))
    portfolio.summary()
  } catch {
    case ex: IllegalArgumentException => println(s"Error in batch operation: ${ex.getMessage}")
  }
}
