# ScalaFinalProject


This is the CSYE7200 Big Data Systems Engineering Using Scala Final Project for Team 3 Fall 2024

Team Members:

Shuyan Bian  bian.shu@northeastern.edu

Xingxing Xiao xiao.xingx@northeastern.edu

Caowang Sun   sun.cao@northeastern.edu

# Final Presentation

# Abstract

To examine a number of different forecasting techniques to predict future stock returns based on past returns and numerical news indicators to construct a portfolio of multiple stocks in order to diversify the risk. We do this by applying supervised learning methods for stock price forecasting by interpreting the seemingly chaotic market data.

# Methodology
## Data cleaning and parsing
1.  Data from all the companies CSV was loaded into dataframes and converted to format required by ARIMA model.
    dataURL: https://www.kaggle.com/datasets/andrewmvd/sp-500-stocks/data

## Spark Timeseries Methodology
1. A time series is a series of data points indexed (or listed or graphed) in time order. Most commonly, a time series is a sequence taken at successive equally spaced points in time.

2. Company name and dates were taken as features to train the data using ARIMA model.

3. Dataframes of all the companies were joined and loaded to RDD.

4. Using ARIMA model data is trained and model is then used for forecasting future values.

5. Using forecast method of ARIMA model stock prices for 30 days.


# Steps to run the project on the local machine
## Run on windows and mac

1. Download sbt 0.1.0-SNAPSHOT

2. Configure Java 1.8 on your machine

3. Configure scala 2.12.11 on your machine

4. To run in IntelliJ.


# Dataset

Alpha Vantage API, for free stock api service covering the majority of our datasets for up to 25 requests per day.

Yahoo Finance Stocks dataset, due to licence issue can't be used.

The dataset was taken from Kaggle and had data for s&p500 companies.

Each data file had 8 columns (Date,Symbol,Adj Close,Close,High,Low,Open,Volume)

We trained the model with the data of 10 companies and 15000 rows.

# Details

utilized Spark to read and pre-process the dataset.

applied Spark-ML to train a Timeseries model.

assessed the accuracy with mae, RMSE, and R square.



# Continuous Integration

This project is using Travis CI as the continuous integration tool  
