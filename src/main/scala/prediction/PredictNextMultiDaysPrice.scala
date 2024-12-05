package prediction

import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.ml.regression.LinearRegression

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.{DoubleType, LongType, StringType, StructField, StructType}




object PredictNextMultiDaysPrice extends App {

  // Number of days to predict, e.g. 7 or 30
  private val predictionDays = 7

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


  // Cast the Volume column from long to double
  val numericData = filledData.withColumn("Volume", col("Volume").cast("double"))

  numericData.printSchema()

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

  // Add initial derived features
  val enrichedData = symbolData
    .withColumn("3DayAvgClose", avg("Close").over(windowSpec.rowsBetween(-2, 0)))
    .withColumn("3DayVolatility", stddev("Close").over(windowSpec.rowsBetween(-2, 0)))
    .na.fill(0.0)

  // Assemble features
  val featureColumns = Array("Open", "High", "Low", "Volume", "3DayAvgClose", "3DayVolatility")
  val assembler = new VectorAssembler()
    .setInputCols(featureColumns)
    .setOutputCol("features")

  // Prepare data for training
  val assembledData = assembler.transform(enrichedData)
    .withColumnRenamed("Close", "label")

  // Train a linear regression model
  val lr = new LinearRegression()
    .setLabelCol("label")
    .setFeaturesCol("features")
  val lrModel = lr.fit(assembledData)


  private val futurePredictions = scala.collection.mutable.ArrayBuffer[Double]()

  // Placeholder to store predictions
  private var recentData = enrichedData.orderBy(desc("Date")).limit(1) // Start with the most recent row


  // Loop to generate predictions for 'predictionDays'
  for (_ <- 1 to predictionDays) {
    // Assemble features for the most recent data
    val assembledRecentData = assembler.transform(recentData).select("features")

    // Predict the next day's close price
    val prediction = lrModel.transform(assembledRecentData)
    val nextClosePrediction = prediction.select("prediction").collect()(0).getDouble(0)

    // Save the predicted value
    futurePredictions += nextClosePrediction

    // Update recentData with the predicted value and recalculate derived features
    recentData = recentData.withColumn("Close", lit(nextClosePrediction))
      .withColumn("3DayAvgClose", avg("Close").over(Window.rowsBetween(-2, 0)))
      .withColumn("3DayVolatility", stddev("Close").over(Window.rowsBetween(-2, 0)))
      .na.fill(0.0)
  }

  // Print predictions for the next `predictionDays`
  println(s"Predicted Close prices for the next $predictionDays days: ${futurePredictions.mkString(", ")}")

  // Stop Spark session
  spark.stop()
}
