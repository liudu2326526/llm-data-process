//package com.llm.spark;
//
//import com.huaban.analysis.jieba.JiebaSegmenter;
//import com.huaban.analysis.jieba.SegToken;
//import com.huaban.analysis.jieba.CharacterUtil;
//import java.util.List;
//
///**
// * @author Liu Du
// * @Date 2024/12/19
// */
//public class JiebaTest {
//
//  public static void main(String[] args) {
//    // 创建 JiebaSegmenter 实例
//    JiebaSegmenter segmenter = new JiebaSegmenter();
//
//    // 输入要分词的中文文本
//    String text = "我爱自然语言[处理]，尤其是中文分词。";
//
//    // 使用 Jieba 分词
//    List<SegToken> tokens = segmenter.process(text, JiebaSegmenter.SegMode.SEARCH);
//
//    // 输出分词结果
//    System.out.println("分词结果：");
//    for (SegToken token : tokens) {
//      String word = token.word;
//
//      // 使用正则表达式去除标点符号
//      if (!word.matches("[\\p{P}\\p{Space}]")) {
//        System.out.println(word);
//      }
////      System.out.println(token.word);
//    }
//  }
//
//}
