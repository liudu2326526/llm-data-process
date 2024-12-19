package com.llm.spark.util;

import com.llm.spark.common.CmdOptions;
import org.apache.spark.sql.SparkSession;

/**
 * @author Liu Du
 * @Date 2024/12/18
 */
public class SparkSessionUtils {

  public static SparkSession initSparkContext(String appName) {
    CmdOptions opts = CmdOptions.getInstance();
    String name = opts.getName();
    if (StringUtils.isNotEmpty(name)) {
      appName = (name + "-" + appName).toLowerCase();
    }
    SparkSession spark = SparkSession.builder()
        .appName(appName)
        .master("local[*]") // 使用本地模式，[*] 表示使用所有可用 CPU 核心
        .getOrCreate();
    return spark;
  }
}
