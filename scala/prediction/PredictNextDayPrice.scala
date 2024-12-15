package prediction

import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.ml.regression.LinearRegression
import org.apache.spark.ml.evaluation.RegressionEvaluator
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.{DoubleType, LongType, StringType, StructField, StructType}



/**
 * Use the historical features (Open, High, Low, Volume) to predict the Close price of the next day.
 */
object PredictNextDayPrice extends App{

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

  // data cleaning: Handle null values by replacing them with 0.0
  val filledData = stockData.na.fill(0.0)

  filledData.printSchema()

  // Cast the Volume column from long to double
  val numericData = filledData.withColumn("Volume", col("Volume").cast("double"))

//  // Select a stock symbol
//  val selectedSymbol = "MMM"
//  //val selectedSymbol = args(0) // Assuming symbol is passed as a command-line argument

  println("Enter the stock symbol you want to predict and evaluate (e.g., MMM, symbol in S&P 500):")
  // Select a stock symbol
  val selectedSymbol = scala.io.StdIn.readLine().toUpperCase()

  // Filter data for the chosen symbol
  val symbolData = numericData.filter(col("Symbol") === selectedSymbol)



  // Define window for ordering by Date
  val windowSpec = Window.partitionBy("Symbol").orderBy("Date")

  // Shift 'Close' column to get the next day's price
  private val dataWithNextClose = symbolData.withColumn("NextClose", lead("Close", 1).over(windowSpec))

  // Filter out rows without labels (last row for each symbol)
  private val validData = dataWithNextClose.filter(col("NextClose").isNotNull)

  // Add optional derived features (moving average and volatility)
  // Moving averages (e.g., 3-day, 7-day), we choose 3-day.
  val enrichedData = validData
    .withColumn("3DayAvgClose", avg("Close").over(windowSpec.rowsBetween(-2, 0)))
    .withColumn("3DayVolatility", stddev("Close").over(windowSpec.rowsBetween(-2, 0)))

  // Fill missing values for derived features
  private val cleanData = enrichedData.na.fill(0.0)

  // Assemble features
  val featureColumns = Array("Open", "High", "Low", "Volume", "3DayAvgClose", "3DayVolatility")
  val assembler = new VectorAssembler()
    .setInputCols(featureColumns)
    .setOutputCol("features")

  val finalData = assembler.transform(cleanData)
    .select("features", "NextClose")
    .withColumnRenamed("NextClose", "label")

  // Split data into training (all rows except last) and test (last row)
  val trainingData = finalData.limit(finalData.count().toInt - 1)
  val testData = finalData.orderBy(desc("Date")).limit(1)

  val lr = new LinearRegression()
    .setLabelCol("label")
    .setFeaturesCol("features")

  val lrModel = lr.fit(trainingData)

  // Predict on the test data
  val predictions = lrModel.transform(testData)

  // Show the predicted and actual price for the next day
  predictions.select("features", "label", "prediction").show()

  // how to evaluate the price's accuracy?
  // Evaluate predictions on test data
  private val evaluatorMAE = new RegressionEvaluator()
    .setLabelCol("label")
    .setPredictionCol("prediction")
    .setMetricName("mae")

  private val evaluatorRMSE = new RegressionEvaluator()
    .setLabelCol("label")
    .setPredictionCol("prediction")
    .setMetricName("rmse")

  private val evaluatorR2 = new RegressionEvaluator()
    .setLabelCol("label")
    .setPredictionCol("prediction")
    .setMetricName("r2")

  // Calculate metrics
  private val mae = evaluatorMAE.evaluate(predictions)
  private val rmse = evaluatorRMSE.evaluate(predictions)
  private val r2 = evaluatorR2.evaluate(predictions)

  println(s"Mean Absolute Error (MAE): $mae")
  println(s"Root Mean Squared Error (RMSE): $rmse")
  println(s"R-squared (R2): $r2")

  // Residuals analysis
//  predictions.columns.foreach(println)
//  val residuals = predictions.withColumn("Residual", col("label") - col("prediction"))
//  residuals.select("Residual").show(5)

  // Stop Spark session
  spark.stop()
}