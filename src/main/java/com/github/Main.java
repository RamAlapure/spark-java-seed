package com.github;

import lombok.extern.slf4j.Slf4j;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import java.util.List;

@Slf4j
public class Main {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setAppName("spark-java-seed").setMaster("local[*]");
        try (JavaSparkContext sparkContext = new JavaSparkContext(conf)) {
            // Create a dummy dataset
            JavaRDD<Integer> dataset = sparkContext.parallelize(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
            // Use the sparkContext for Spark operations
            Integer sum = dataset.reduce(Integer::sum);
            log.info("Sum: {}", sum);
        }
    }
}