package com.llm.spark.process;

import com.llm.spark.common.CmdOptions;
import com.llm.spark.util.IOUtils;
import com.llm.spark.util.SparkSessionUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;

/**
 * @author Liu Du
 * @Date 2024/12/18
 */
@Slf4j
public class SimHashDistinctProcess {

  public static void main(String[] args) {
    CmdOptions.init(args);
    SparkSession ss = SparkSessionUtils
        .initSparkContext(SimHashDistinctProcess.class.getSimpleName());
    try {
      CmdOptions options = CmdOptions.getInstance();
      run(ss, options.getPartition(), options.getInputLocation(), options.getOutputLocation());
    } catch (Exception e) {
      log.error("run processor error", e);
      System.exit(1);
    } finally {
      ss.stop();
    }
  }

  private static void run(SparkSession ss, int partition, String input,
      String output) {
    StructType schema = new StructType()
        .add("udid", DataTypes.StringType, false);
    Dataset<Row> s = ss.read().options(IOUtils.getCSVOption()).schema(schema).csv(input)
        .repartition(partition);
    s.show();

  }

}
