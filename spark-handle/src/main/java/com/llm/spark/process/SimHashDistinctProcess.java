package com.llm.spark.process;

import com.llm.spark.common.CmdOptions;
import com.llm.spark.util.IOUtils;
import com.llm.spark.util.SimHashUtil;
import com.llm.spark.util.SparkSessionUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.ml.feature.MinHashLSH;
import org.apache.spark.ml.feature.MinHashLSHModel;
import org.apache.spark.ml.linalg.Vector;
import org.apache.spark.ml.linalg.VectorUDT;
import org.apache.spark.ml.linalg.Vectors;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
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
      run(ss, options.getFormat(), options.getInputLocation(), options.getOutputLocation());
    } catch (Exception e) {
      log.error("run processor error", e);
      System.exit(1);
    } finally {
      ss.stop();
    }
  }

  private static void run(SparkSession ss, String format, String input,
      String output) {
    Dataset<Row> s = null;
    switch (format) {
      case "parquet":
        s = ss.read().parquet(input);
        break;
      case "orc":
        s = ss.read().orc(input);
        break;
      case "json":
        s = ss.read().json(input);
        break;
      case "csv":
        StructType schema = new StructType()
            .add("data", DataTypes.StringType);
        s = ss.read().options(IOUtils.getCSVOption()).schema(schema).csv(input);
        break;
    }

    Dataset<Row> content = s.select("content").where("content is not null and content != ''");

    // 2. 使用 SimHash 计算文本特征
    JavaRDD<Row> simhashRDD = content.toJavaRDD().map(row -> {
      String text = row.getString(0); // 假设第一列为文本
      // 使用 SimHash 计算 SimHash 值
      long simhash = SimHashUtil.computeSimHash(text);
      return RowFactory.create(text, simhash);
    });

    // 定义新的 schema

    // 转换为 DataFrame

    Dataset<Row> simhashDF = ss.createDataFrame(simhashRDD, new StructType()
        .add("text", DataTypes.StringType)
        .add("simhash", DataTypes.LongType)
    );

    simhashDF.show(10);

    // 将 SimHash 转换为 Binary Vector 用于 MinHash
    JavaRDD<Row> binaryVectorRDD = simhashDF.toJavaRDD().map(row -> {
      // 创建一个长度为64的数组来存储每一位的值
      double[] values = new double[64];
      long simhash = row.getLong(1);
      // 将long值的每一位转化为数组的元素
      for (int i = 0; i < 64; i++) {
        values[i] = (simhash >> (63 - i)) & 1;  // 获取每一位并赋值
      }

      Vector vector = Vectors.dense(values);
      return RowFactory.create(row.getString(0), vector);
    });

    Dataset<Row> binaryVectorDF = ss.createDataFrame(binaryVectorRDD, new StructType()
        .add("text", DataTypes.StringType)
        .add("features", new VectorUDT()));
    binaryVectorDF.show(10);

    MinHashLSH mhLSH = new MinHashLSH()
        .setInputCol("features")
        .setOutputCol("hashes")
        .setNumHashTables(100);  // 设置哈希表数量（一般来说越多越精确，但也会增加计算量）

    MinHashLSHModel model = mhLSH.fit(binaryVectorDF);
    Dataset<Row> transformDF = model.transform(binaryVectorDF);

    transformDF.show(10);
    // 使用 approxSimilarityJoin 进行去重操作
    Dataset<Row> select = model.approxSimilarityJoin(binaryVectorDF, binaryVectorDF, 1.0,
            "distance")
        .select(
            "datasetA.text",
            "datasetB.text",
            "datasetA.hashes",
            "datasetB.hashes",
            "distance"
        ).where("distance < 0.2 and datasetA.text != datasetB.text");

    select.show(10);
//
//    // 过滤掉自己和自己的比较
//    Dataset<Row> distinctResult = result.filter("datasetA.text != datasetB.text");

  }

}
