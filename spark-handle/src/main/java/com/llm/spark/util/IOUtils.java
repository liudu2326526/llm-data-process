package com.llm.spark.util;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.spark.sql.DataFrameWriter;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;

/**
 * @author Liu Du
 * @Date 2024/12/18
 */
public class IOUtils {

  public static final String PATH_SEPARATOR = "#";

  public static Map<String, String> getCSVOption() {
    Map<String, String> o = new HashMap<>();
    o.put("compression", "gzip");
    o.put("nullValue", "null");

    return o;
  }

  public static void saveDataset(Dataset<Row> ds, String output) {
    ds.write().mode(SaveMode.Overwrite).options(getCSVOption()).csv(output);
  }

  public static void saveDatasetAsOrc(Dataset<Row> ds, String output, String partitionKey) {
    DataFrameWriter<Row> writer = ds.write().mode(SaveMode.Overwrite);
    if (StringUtils.isNotEmpty(partitionKey)) {
      String[] keys = partitionKey.split(",");
      writer = writer.partitionBy(keys);
    }
    writer.orc(output);
  }

  /**
   * @param input can be multiple locations, join with '#'
   */
  public static Dataset<Row> loadDataset(SparkSession ss, String input) {
    String[] inputs = input.split(PATH_SEPARATOR);
    return ss.read().options(getCSVOption()).csv(inputs);
  }

  public static Dataset<Row> loadDatasetFromOrc(SparkSession ss, String input) {
    String[] inputs = input.split(PATH_SEPARATOR);
    return ss.read().orc(inputs);
  }

  public static void closeQuietly(AutoCloseable closeable) {
    try {
      if (closeable != null) {
        closeable.close();
      }
    } catch (Exception e) {

    }
  }
}
