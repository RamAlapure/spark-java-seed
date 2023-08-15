package com.github.client;

import com.github.model.StockPrice;
import com.github.sh0nk.matplotlib4j.Plot;
import com.github.sh0nk.matplotlib4j.PythonExecutionException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
public class AlphaVantageClient {

    public static final String BASE_URL = "https://www.alphavantage.co/query";
    public static final String API_KEY = "Q0MXTBS7LMVP6X1E";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @SuppressWarnings("unchecked")
    public static List<StockPrice> fetchDailyPrice(String symbol) throws IOException, ParseException {
        String url = "%s?function=TIME_SERIES_DAILY&symbol=%s&apikey=%s&outputsize=full".formatted(BASE_URL, symbol, API_KEY);
        Response response = get(url);
        JSONParser parser = new JSONParser();
        assert response.body() != null;
        JSONObject jsonObject = (JSONObject) parser.parse(response.body().string());
        JSONObject timeSeries = (JSONObject) jsonObject.get("Time Series (Daily)");
        return timeSeries.keySet().stream().map(key -> composeStockPrice(symbol, (String) key, timeSeries)).toList();
    }

    public static StockPrice composeStockPrice(String symbol, String key, JSONObject timeSeries) {
        LocalDate date = LocalDate.parse(key, DATE_FORMATTER);
        JSONObject data = (JSONObject) timeSeries.get(key);
        double price = Double.parseDouble((String) data.get("4. close"));
        return new StockPrice(symbol, date, price);
    }

    public static Response get(String url) throws IOException {
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        Request request = new Request.Builder().url(url).get().build();
        return client.newCall(request).execute();
    }

    public static void main(String[] args) throws IOException, ParseException, PythonExecutionException {
        // Create a dummy dataset
        List<StockPrice> list = fetchDailyPrice("AAPL");
        Plot plt = Plot.create();
        List<Long> dates = list.stream().map(StockPrice::getDate).map(LocalDate::toEpochDay).toList();
        List<Double> prices = list.stream().map(StockPrice::getPrice).toList();
        plt.plot().add(dates, prices);

        // Set the chart title and axis labels
        plt.title("Stock Price Chart");
        plt.xlabel("Date");
        plt.ylabel("Price");

        plt.show();
    }

    private static void jfree(List<StockPrice> list) throws IOException {
        // Convert the selected data to a JFreeChart dataset
        DefaultCategoryDataset chartDataset = new DefaultCategoryDataset();
        list.forEach(row -> {
            LocalDate xValue = row.getDate();
            double yValue = row.getPrice();
            chartDataset.addValue(yValue, "Series 1", xValue);
        });
        // Create the line chart
        JFreeChart chart = ChartFactory.createLineChart(
                "Line Chart", // Chart title
                "Datetime", // X-axis label
                "Price", // Y-axis label
                chartDataset, // Dataset
                PlotOrientation.VERTICAL,
                true, // Include legend
                true, // Include tooltips
                false // Include URLs
        );
        ChartUtils.saveChartAsPNG(new File("chart.png"), chart, 800, 600);
    }
}