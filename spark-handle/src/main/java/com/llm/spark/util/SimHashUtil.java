package com.llm.spark.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;

/**
 * @author Liu Du
 * @Date 2024/12/18
 */
public class SimHashUtil {

  public static final int HASH_BITS = 64; // SimHash 的位数

  /**
   * 计算文本的 SimHash 值
   *
   * @param text 输入的文本
   * @return 64 位 SimHash 值
   * @throws Exception 如果哈希计算失败
   */
  public static long computeSimHash(String text) throws Exception {
    // 分词并生成特征（简单处理，权重设为1）
    List<String> words = tokenize(text);
    Map<String, Integer> features = new HashMap<>();
    for (String word : words) {
      features.put(word, features.getOrDefault(word, 0) + 1);
    }

    // 初始化向量
    int[] v = new int[HASH_BITS];

    // 遍历特征并加权哈希
    for (Map.Entry<String, Integer> entry : features.entrySet()) {
      String feature = entry.getKey();
      int weight = entry.getValue();

      // 对特征进行哈希并转为二进制表示
      long hash = hash(feature);
      for (int i = 0; i < HASH_BITS; i++) {
        long bitmask = 1L << i;
        if ((hash & bitmask) != 0) {
          v[i] += weight; // 哈希位为1，加权
        } else {
          v[i] -= weight; // 哈希位为0，减权
        }
      }
    }

    // 生成 SimHash
    long simHash = 0;
    for (int i = 0; i < HASH_BITS; i++) {
      if (v[i] > 0) {
        simHash |= (1L << i); // 向量值大于0，对应位为1
      }
    }
    return simHash;
  }

  /**
   * 对字符串进行哈希计算，返回 64 位长整型
   *
   * @param input 输入字符串
   * @return 64 位哈希值
   * @throws Exception 如果哈希计算失败
   */
  private static long hash(String input) throws Exception {
    MessageDigest md = MessageDigest.getInstance("MD5");
    byte[] digest = md.digest(input.getBytes());
    BigInteger bi = new BigInteger(1, digest);
    return bi.longValue(); // 截取低 64 位
  }

  /**
   * 简单的分词函数
   *
   * @param text 输入文本
   * @return 分词结果
   */
  private static List<String> tokenize(String text) {
    // 这里使用简单的空格分词，可以替换为更复杂的分词器（如 Jieba 或 HanLP）
    List<String> collect = ToAnalysis.parse(text).getTerms().stream().map(Term::getName)
        .collect(Collectors.toList());
    if (collect.size()==0){
      return Collections.singletonList("");
    }
    return collect;
  }

//  /**
//   * 简单的分词函数
//   *
//   * @param text 输入文本
//   * @return 分词结果
//   */
//  private static List<String> tokenize(String text) {
//    // 这里使用简单的空格分词，可以替换为更复杂的分词器（如 Jieba 或 HanLP）
//    JiebaSegmenter segmenter = new JiebaSegmenter();
//
//    List<String> collect = segmenter.process(text, SegMode.SEARCH).stream()
//        .map(x -> x.word)
//        .filter(word -> !word.matches("[\\p{P}\\p{Space}]"))
//        .collect(Collectors.toList());
//
//    if (collect.size() == 0) {
//      return Collections.singletonList("");
//    }
//    return collect;
//  }

  public static void main(String[] args) {
    try {
      String text = "北京真好玩[爱你]";
      long simHash = computeSimHash(text);
      System.out.println("SimHash 值: " + simHash);
      System.out.println("SimHash（二进制）: " + Long.toBinaryString(simHash));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
