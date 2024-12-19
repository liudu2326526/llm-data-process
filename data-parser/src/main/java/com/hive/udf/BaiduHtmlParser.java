package com.hive.udf;

import com.fasterxml.jackson.databind.JsonNode;
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
 * @Date 2024/12/18
 */
public class BaiduHtmlParser extends UDF {

  public static String evaluate(String content) {
    try {
      return processHtmlFile(content);
    } catch (IOException e) {
      e.printStackTrace();
      return "";
    }
  }

  public static String processHtmlFile(String content) throws IOException {
    // 读取和解析HTML内容
    Document doc = Jsoup.parse(content, "UTF-8");
    ObjectMapper objectMapper = new ObjectMapper();

    Map<String, Object> data = new HashMap<>();

    // 创建存储基本信息的 Map
    Map<String, String> basicInfo = new HashMap<>();

    // 查找基本信息部分
    Element basicInfoSection = doc.selectFirst("div.basicInfo_pXBZ7.J-basic-info");

    if (basicInfoSection != null) {
      // 遍历所有条目
      Elements items = basicInfoSection.select("div.itemWrapper_CR0U3");
      for (Element item : items) {
        Element keyElement = item.selectFirst("dt.basicInfoItem_vbiBk.itemName_fOdwv");
        Element valueElement = item.selectFirst("dd.basicInfoItem_vbiBk.itemValue_JaQOj");

        if (keyElement != null && valueElement != null) {
          String keyText = keyElement.text().trim();
          StringBuilder valueText = new StringBuilder();

          // 拼接所有 span 和 a 标签中的文本
          Elements spansAndLinks = valueElement.select("span, a");
          for (Element spanOrLink : spansAndLinks) {
            valueText.append(spanOrLink.text().trim());
          }

          // 存入 Map
          basicInfo.put(keyText, valueText.toString());
        }
      }
    }

    data.put("info", basicInfo);

    // ===== 提取页面标题 =====
    String title = doc.title();
    data.put("title", title);

    // ===== 提取 meta 描述和关键词 =====
    Element metaDescription = doc.selectFirst("meta[name=description]");
    Element metaKeywords = doc.selectFirst("meta[name=keywords]");
    data.put("description", metaDescription != null ? metaDescription.attr("content") : null);
    data.put("keywords", metaKeywords != null ? metaKeywords.attr("content") : null);

    // ===== 提取目录数据 =====
    List<Map<String, Object>> catalog = new ArrayList<>();
    Elements scriptTags = doc.select("script");
    for (Element scriptTag : scriptTags) {
      String scriptContent = scriptTag.html();
      if (scriptContent.contains("\"catalog\":")) {
        int startIndex = scriptContent.indexOf("\"catalog\":");
        int endIndex = scriptContent.indexOf("]", startIndex) + 1;
        String catalogJson = scriptContent.substring(startIndex, endIndex);
        JsonNode rootNode = objectMapper.readTree("{" + catalogJson + "}");
        JsonNode catalogNode = rootNode.get("catalog");
        if (catalogNode != null) {
          for (JsonNode item : catalogNode) {
            Map<String, Object> catalogItem = new HashMap<>();
            catalogItem.put("title", item.get("title").asText());
            catalogItem.put("index", item.get("index").asInt());
            catalog.add(catalogItem);
          }
        }
        break;
      }
    }

    // ===== 解析段落和索引 =====
    Map<String, List<String>> paragraphsByPrefix = new HashMap<>();
    Map<String, List<String>> indicesByPrefix = new HashMap<>();

    Elements divs = doc.select("div.para_G3_Os.content_cFweI.MARK_MODULE");
    for (Element div : divs) {
      StringBuilder paragraphText = new StringBuilder();
      Elements spans = div.select("span.text_B_eob");
      for (Element span : spans) {
        paragraphText.append(span.text().trim());
      }

      String dataIdx = div.attr("data-idx");
      if (dataIdx != null && !dataIdx.isEmpty()) {
        String prefix = dataIdx.split("-")[0];
        paragraphsByPrefix.putIfAbsent(prefix, new ArrayList<>());
        indicesByPrefix.putIfAbsent(prefix, new ArrayList<>());
        paragraphsByPrefix.get(prefix).add(paragraphText.toString());
        indicesByPrefix.get(prefix).add(dataIdx);
      }
    }

    // 合并段落
    Map<String, String> mergedParagraphs = new HashMap<>();
    for (Map.Entry<String, List<String>> entry : paragraphsByPrefix.entrySet()) {
      mergedParagraphs.put(entry.getKey(), String.join("\n", entry.getValue()));
    }

    // ===== 构建标题和段落的映射 =====
    Map<String, String> titleMap = new HashMap<>();
    for (Map<String, Object> item : catalog) {
      String indexKey = String.valueOf((int) item.get("index") - 1);
      if (mergedParagraphs.containsKey(indexKey)) {
        titleMap.put((String) item.get("title"), mergedParagraphs.get(indexKey));
      }
    }

    data.put("message", titleMap);

    // 返回最终的标题和段落映射
    return objectMapper.writeValueAsString(data);
  }

//  public static void main(String[] args) throws IOException {
//    String filePath = "/Users/macbook/Downloads/百度百科_白森林品牌.html";  // 替换为文件路径
//    String content = new String(Files.readAllBytes(Paths.get(filePath)));
//    String titleMap = evaluate(content);
//
//    System.out.println(titleMap);
//  }
}
