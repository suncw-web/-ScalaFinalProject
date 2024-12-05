package prediction



import org.apache.spark.ml.evaluation.RegressionEvaluator
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.ml.regression.LinearRegression
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._

/**
 * This method is to use 'Open', 'High', 'Low', and 'Volume' to predict 'Close' price of the same day
 * I create another method to predict the next day's 'close' price by linear regression
 */
object PredictOneStockPrice {
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
    val filePath = "src/main/resources/sp500_stocks.csv"
    val stockData = spark.read
      .option("header", "true")
      .option("inferSchema", "true")
      .schema(schema)
      .csv(filePath)

    // Get the total number of rows in the dataset
    val totalRows = stockData.count()
    println(s"Total number of rows in the dataset: $totalRows")

    // data cleaning: Handle null values by replacing them with 0.0
    val filledData = stockData.na.fill(0.0)

    filledData.printSchema()

    // Cast the Volume column from long to double
    val numericData = filledData.withColumn("Volume", col("Volume").cast("double"))

    println("Enter the stock symbol you want to predict and evaluate (e.g., MMM, symbol in S&P 500):")
    // Select a stock symbol
    val selectedSymbol = scala.io.StdIn.readLine().toUpperCase()

    // Filter data for the chosen symbol
    val symbolData = numericData.filter(col("Symbol") === selectedSymbol)

    // Feature Engineering: Use 'Open', 'High', 'Low', and 'Volume' to predict 'Close'
    val featureColumns = Array("Open", "High", "Low", "Volume")
    val assembler = new VectorAssembler()
      .setInputCols(featureColumns)
      .setOutputCol("features")

    // Transform the data
    val assembledData = assembler.transform(symbolData)

    // Prepare the final dataset
    val finalData = assembledData.select("features", "Close")
    finalData.show(5)

    // Split data into training (80%) and test (20%) sets
    val Array(trainingData, testData) = finalData.randomSplit(Array(0.8, 0.2))

    // Train the model
    val lr = new LinearRegression()
      .setLabelCol("Close")
      .setFeaturesCol("features")
    val lrModel = lr.fit(trainingData)

    // Make predictions on the test data
    val predictions = lrModel.transform(testData)

    // Evaluate the model
    val evaluator = new RegressionEvaluator()
      .setLabelCol("Close")
      .setPredictionCol("prediction")
      .setMetricName("rmse")

    val rmse = evaluator.evaluate(predictions)
    println(s"Root Mean Squared Error (RMSE) for $selectedSymbol: $rmse")

    // Display predictions
    predictions.select("features", "Close", "prediction").show(5)

    // Stop Spark session
    spark.stop()
  }
}

