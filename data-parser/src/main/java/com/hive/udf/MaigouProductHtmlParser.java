package com.hive.udf;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
public class MaigouProductHtmlParser extends UDF {

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

    Element name = doc.selectFirst("header div.headlist div.sublogo");
    if (name != null) {
      data.put("name", name.text());
    }

//    解析行业
    Element industryDiv = doc.selectFirst(
        "div.itembox[paramstr*=brand_searchproduct] dl.licont.catid dd");

    if (industryDiv != null) {
      // 创建列表存储行业名称
      List<String> industryList = new ArrayList<>();

      // 获取所有行业的 <span> 标签内容
      Elements industries = industryDiv.select("span.leval");
      for (Element industry : industries) {
        String industryName = industry.text();
        industryList.add(industryName);
      }
      data.put("industry", industryList);
    }

    // 解析产品
    Element productListDiv = doc.selectFirst("div.blockinfo.productlist1");
    List<Map<String, Object>> productList = new ArrayList<>();

    Elements items = productListDiv.select("div.item");
    for (Element item : items) {
      Map<String, Object> productInfo = new HashMap<>();

      // 产品名称
      Element titleElement = item.selectFirst("a.title");
      String productName = titleElement != null ? titleElement.text() : "未知";
      productInfo.put("产品名称", productName);

      ArrayList<String> tags = new ArrayList<>();
      // 产品标签
      Elements tagElements = item.select("div.biaoqianlist span.font14");
      for (Element tagElement : tagElements) {
        tags.add(tagElement.text());
      }
      if (tags.size() > 0) {
        productInfo.put("标签", tags);
      }

      // 品牌名称
      Element brandElement = item.selectFirst("li strong:contains(所属品牌) + a");
      if (brandElement != null) {
        productInfo.put("品牌名称", brandElement.text());
      }

      // 参考价格
      Element priceElement = item.selectFirst("li strong:contains(参考价格) + a span.font22");
      if (priceElement != null) {
        productInfo.put("参考价格", priceElement.text());
      }

      // 推荐理由
      Element recommendationElement = item.selectFirst("div.ctxt");
      if (recommendationElement != null) {
        Element recommendationStrongElement = recommendationElement.selectFirst("strong");
        Element recommendationAElement = recommendationElement.selectFirst("a");
        String recommendation = recommendationElement.text();
        if (recommendationAElement != null) {
          recommendation = recommendation.replace(recommendationAElement.text(), "");
        }
        if (recommendationStrongElement != null) {
          recommendation = recommendation.replace(recommendationStrongElement.text(), "");
          productInfo.put(recommendationStrongElement.text().replace("：", ""), recommendation);
        }
      }

      // 产品型号
      Element modelElement = item.selectFirst("li strong:contains(产品型号) + span");
      if (modelElement != null) {
        productInfo.put("产品型号", modelElement.text());
      }

      // 添加到列表
      productList.add(productInfo);
    }
    data.put("product", productList);

    return objectMapper.writeValueAsString(data);
  }


//  public static void main(String[] args) throws IOException {
//    String filePath = "/Users/macbook/IdeaProjects/llm-data-process/data-parser/src/main/resources/买购品牌.html";  // 替换为文件路径
//    String content = new String(Files.readAllBytes(Paths.get(filePath)));
//    String titleMap = evaluate(content);
//
//    System.out.println(titleMap);
//  }

}
