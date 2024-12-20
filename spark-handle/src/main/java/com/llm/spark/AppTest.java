package com.llm.spark;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

/**
 * @author Liu Du
 * @Date 2024/12/18
 */
public class AppTest {

  public static void main(String[] args) {
    // 初始化 SparkSession
    SparkSession spark = SparkSession.builder()
        .master("local[*]") // 使用本地模式，[*] 表示使用所有可用 CPU 核心
        .getOrCreate();

    // 创建一个简单的 DataFrame
    Dataset<Row> data = spark.read().parquet("/Users/macbook/Downloads/4ed6280789a878d31f005ba5c6ab6055.parquet");
    data.schema().printTreeString();
    // 展示 DataFrame 内容
    data.show();
    data = data.select("raw_data");
    data.write().text("/Users/macbook/Downloads/parser_test.txt");

    // 停止 SparkSession
    spark.stop();
  }
}
