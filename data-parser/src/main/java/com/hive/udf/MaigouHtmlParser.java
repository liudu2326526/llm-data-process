package com.hive.udf;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.hadoop.hive.ql.exec.UDF;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * @author Liu Du
 * @Date 2024/12/19
 */
public class MaigouHtmlParser extends UDF {

  public static String evaluate(String content) {
    try {
      return processHtmlFile(content);
    } catch (Exception e) {
      e.printStackTrace();
      return "";
    }
  }

  public static String processHtmlFile(String content) throws Exception {
    Map<String, Object> data = new HashMap<>();
    ObjectMapper objectMapper = new ObjectMapper();
    // 解析HTML
    Document doc = Jsoup.parse(content, "UTF-8");

    ArrayList<String> tags = new ArrayList<>();
    // 解析标签
    Elements bqianElements = doc.select("div.rongyulist div.bqian");

    // 提取每个div.bqian的文字
    for (Element element : bqianElements) {
      // 获取span标签中的文本内容
      String text = element.select("span").text();

      // 如果没有span标签，尝试从a标签中提取文本
      if (text.isEmpty()) {
        text = element.select("a span").text();
      }

      // 输出提取的文本
      if (!text.isEmpty()) {
        tags.add(text);
      }
    }

    data.put("tags", tags);

    // 获取 .message 下的品牌名 (h1标签)
    String brandName = doc.select("div.message h1").text();
    data.put("name", brandName);

    // 获取 .message 下的公司名 (a标签)
    String companyName = doc.select("div.message div.bottom a.fff").text();
    data.put("company", companyName);

    // 获取 .message 下的电话 (div.li标签)
    Elements informations = doc.select("div.message div.bottom div.li");
    for (Element information : informations) {
      if (information.text().startsWith("电话")){
        data.put("phone", information.text().replace("电话：",""));
      } else if (information.text().startsWith("邮箱")){
        data.put("email", information.text().replace("邮箱：",""));
      }
    }


    // 获取品牌宣传语
    String brandSlogan = doc.select("div.blockinfo.brandintroduce div.blocktitle .subtitle em")
        .text();
    data.put("slogan", brandSlogan);

    // 获取品牌企业介绍
    String brandDescription = doc.select("div.branddesc .desc").text();
    data.put("desc", brandDescription);

    // 创建一个 Map 来存储信息
    Map<String, String> messageMap = new HashMap<>();

    // 获取基本信息部分
    Elements tableRows = doc.select("div.itembox .tablelist .tdleft");

    // 循环遍历所有的 .tdleft 和 .tdright
    for (Element tdLeft : tableRows) {
      // 获取左边的文本（例如 "注册资本"）
      String key = tdLeft.text().trim();

      // 获取右边对应的值（例如 "28000万元"）
      Element tdRight = tdLeft.nextElementSibling();
      String value = tdRight != null ? tdRight.text().trim() : "";

      // 将键值对添加到 Map 中
      if (!key.isEmpty() && !value.isEmpty()) {
        messageMap.put(key, value);
      }
    }
    data.put("message", messageMap);

    return objectMapper.writeValueAsString(data);
  }


//  public static void main(String[] args) throws IOException {
//    String filePath = "/Users/macbook/IdeaProjects/llm-data-process/data-parser/src/main/resources/肯帝亚-买购网.html";  // 替换为文件路径
//    String content = new String(Files.readAllBytes(Paths.get(filePath)));
//    String titleMap = evaluate(content);
//
//    System.out.println(titleMap);
//  }

}
