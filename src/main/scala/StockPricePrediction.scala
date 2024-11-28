import org.apache.spark.sql.{SparkSession, Row}
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.ml.regression.LinearRegression
import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.ml.evaluation.RegressionEvaluator


object StockPricePrediction {

  def main(args: Array[String]): Unit = {
    // Initialize Spark session
    val spark = SparkSession.builder()
      .appName("StockPricePrediction")
      .master("local[*]") // Use local mode, change if running in a cluster
      .getOrCreate()

    // Define schema for the input CSV data
    val schema = StructType(Seq(
      StructField("Date", StringType, nullable = true),
      StructField("Symbol", StringType, nullable = true),
      StructField("Adj Close", DoubleType, nullable = true),
      StructField("Close", DoubleType, nullable = true),
      StructField("High", DoubleType, nullable = true),
      StructField("Low", DoubleType, nullable = true),
      StructField("Open", DoubleType, nullable = true),
      StructField("Volume", LongType, nullable = true)
    ))

    // Load the dataset
    val filePath = "src/main/resources/sp500_stocks.csv" // Replace with your actual file path
    val stockData = spark.read
      .option("header", "true")
      .option("inferSchema", "true")
      .schema(schema)
      .csv(filePath)

    // Display the first few rows of the data for inspection
    stockData.show(5)

    stockData.printSchema()
    stockData.show(10)

    // data cleaning: Handle null values by replacing them with 0.0
    val filledData = stockData.na.fill(0.0)

    filledData.printSchema()

    // Cast the Volume column from long to double
    val numericData = filledData.withColumn("Volume", col("Volume").cast("double"))

    // Get distinct symbols
    val symbols = numericData.select("Symbol").distinct().collect().map(_.getString(0))

    // Iterate over each symbol and perform prediction
    symbols.foreach { symbol =>
      println(s"Processing Symbol: $symbol")

    // Filter data for the current symbol
    val symbolData = numericData.filter(col("Symbol") === symbol)


      // Feature Engineering: Let's use the 'Open', 'High', 'Low', and 'Volume' columns to predict the 'Close' price
      val featureColumns = Array("Open", "High", "Low", "Volume")
      val assembler = new VectorAssembler()
        .setInputCols(featureColumns)
        .setOutputCol("features")

      // Prepare the data for regression by transforming it using the assembler
      //Transform the dataset
      val assembledData = assembler.transform(symbolData)

      // Show the resulting features column
      assembledData.select("features").show(10, truncate = false)

      // Prepare the final dataset with features and label (close price)
      val finalData = assembledData.select("features", "Close")

      // Split data into training (80%) and test (20%) sets
      val Array(trainingData, testData) = finalData.randomSplit(Array(0.8, 0.2))

      // Create a linear regression model
      val lr = new LinearRegression()
        .setLabelCol("Close")
        .setFeaturesCol("features")

      // Train the model on the training data
      val lrModel = lr.fit(trainingData)

      //    // Print the coefficients and intercept of the trained model
      //    println(s"Coefficients: ${lrModel.coefficients}")
      //    println(s"Intercept: ${lrModel.intercept}")

      // Print model coefficients and intercept
      println(s"Coefficients for $symbol: ${lrModel.coefficients}")
      println(s"Intercept for $symbol: ${lrModel.intercept}")

      // Make predictions on the test data
      val predictions = lrModel.transform(testData)

      // Show the predicted and actual close prices (default show 20 rows)
      predictions.select("Symbol","features", "Close", "prediction").show(5)

      // Evaluate the model using regression evaluator
      val evaluator = new RegressionEvaluator()
        .setLabelCol("Close")
        .setPredictionCol("prediction")
        .setMetricName("rmse") // Root Mean Squared Error

      val rmse = evaluator.evaluate(predictions)
      println(s"Root Mean Squared Error (RMSE) on test data: $rmse")
    }

    // Stop Spark session
    spark.stop()
  }
}
