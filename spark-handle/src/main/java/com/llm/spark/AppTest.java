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
    Dataset<Row> data = spark.read().json("/Users/macbook/IdeaProjects/llm-data-process/spark-handle/src/main/resources/sample.json");

    // 展示 DataFrame 内容
    data.show();

    // 进行简单的查询
    data.select("name").where("age > 20").show();

    // 停止 SparkSession
    spark.stop();
  }
}
