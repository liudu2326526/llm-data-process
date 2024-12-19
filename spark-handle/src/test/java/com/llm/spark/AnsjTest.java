package com.llm.spark;

import java.io.InputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ansj.domain.Result;
import org.ansj.domain.Term;
import org.ansj.library.UserDefineLibrary;
import org.ansj.splitWord.analysis.BaseAnalysis;
import org.ansj.splitWord.analysis.NlpAnalysis;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.nlpcn.commons.lang.tire.domain.Forest;
import org.nlpcn.commons.lang.tire.domain.Value;
import org.nlpcn.commons.lang.tire.library.Library;

/**
 * @author Liu Du
 * @Date 2024/12/18
 */
public class AnsjTest {

  public static void main(String[] args) {
    // dynamicWord();
    // localDic();
     moreUserDic();
  }

  /**
   * 多用户词典(新增、删除)
   */
  public static void moreUserDic() {
    // 多用户词典
    String str = "神探夏洛克这部[电影]作者.是一个dota迷";
    Result parse = ToAnalysis.parse(str);
    for (Term term : parse) {
      System.out.println(term.getName());
      System.out.println(term.getNatureStr());
    }
//    System.out.println(parse);
    // 两个词汇 神探夏洛克 douta迷
//    Forest dic1 = new Forest();
//    Library.insertWord(dic1, new Value("神探夏洛克", "define", "1000"));
//    Forest dic2 = new Forest();
//    Library.insertWord(dic2, new Value("dota迷", "define", "1000"));
//    System.out.println(ToAnalysis.parse(str, dic1, dic2));
//
//    System.out.println("-------删除dic1中的词");
//    Library.removeWord(dic1, "神探夏洛克");
//    System.out.println(ToAnalysis.parse(str, dic1, dic2));
  }

  /**
   * 动态增删词库
   */
  public static void dynamicWord() {
    // 增加新词,中间按照'\t'隔开
    UserDefineLibrary.insertWord("ansj中文分词", "userDefine", 1000);
    Result result = ToAnalysis.parse("我觉得Ansj中文分词是一个不错的系统!我是王婆!");
    System.out.println("增加新词例子:" + result);
    // 删除词语,只能删除.用户自定义的词典.
    UserDefineLibrary.removeWord("ansj中文分词");
    result = ToAnalysis.parse("我觉得ansj中文分词是一个不错的系统!我是王婆!");
    System.out.println("删除用户自定义词典例子:" + result);

    //将用户自定义词典清空
    UserDefineLibrary.clear();
  }

  /**
   * 加载词典文件
   */
  public static void localDic() {
    try {
      //读的是根目录下的
      Forest rootForest = Library.makeForest("library/userLibrary.dic");
      System.out.println(rootForest.toMap());
      //加载字典文件,取的是resource下的
      InputStream inputStream = AnsjTest.class.getResourceAsStream("/library/userLibrary.dic");
      Forest resoutceForest = Library.makeForest(inputStream);
      String str = "我觉得ansj中文分词是一个不错的系统!我是王婆!";
      Result result = ToAnalysis.parse(str, resoutceForest);//传入forest
      List<Term> termList = result.getTerms();
      for (Term term : termList) {
        System.out.println(term.getName() + ":" + term.getNatureStr());
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  /**
   * 基本分词
   * 基本就是保证了最基本的分词.词语颗粒度最非常小的..所涉及到的词大约是10万左右.
   * 基本分词速度非常快.在macAir上.能到每秒300w字每秒.同时准确率也很高.但是对于新词他的功能十分有限
   *
   * @param content
   */
  public static void baseAnay(String content) {
    Result result = BaseAnalysis.parse(
        delHTMLTag(content).replace("\n", "").replace(" ", "").replace("\t", ""));
    System.out.println("result:" + result);
  }

  /**
   * 精准分词
   * 它在易用性,稳定性.准确性.以及分词效率上.都取得了一个不错的平衡.
   *
   * @param content
   */
  public static void toAnay(String content) {
    Result result = ToAnalysis.parse(content);
    System.out.println("result:" + result);
  }

  /**
   * nlp分词(单条新闻处理7秒)
   * 可以识别出未登录词.但是它也有它的缺点.速度比较慢.稳定性差.ps:我这里说的慢仅仅是和自己的其他方式比较.应该是40w字每秒的速度吧.
   * 个人觉得nlp的适用方式.1.语法实体名抽取.未登录词整理.只要是对文本进行发现分析等工作
   * 会把企业分开
   *
   * @param content
   */
  public static void nlpAnay(String content) {
    Result result = NlpAnalysis.parse(
        delHTMLTag(content).replace("\n", "").replace(" ", "").replace("\t", ""));
    System.out.println("result:" + result);
    List<Term> terms = result.getTerms();
    for (Term term : terms) {
      String name = term.getName();
      String nature = term.getNatureStr();
      if (nature.equals("nt") || nature.equals("nr")) {
        System.out.println("------------------");
        System.out.println("getName:" + term.getName());
        System.out.println("getNatureStr:" + term.getNatureStr());
      }
    }
  }

  /**
   * 筛除HTML标签
   *
   * @param htmlStr
   * @return
   */
  public static String delHTMLTag(String htmlStr) {
    String regEx_script = "<script[^>]*?>[\\s\\S]*?<\\/script>"; //定义script的正则表达式
    String regEx_style = "<style[^>]*?>[\\s\\S]*?<\\/style>"; //定义style的正则表达式
    String regEx_html = "<[^>]+>"; //定义HTML标签的正则表达式

    Pattern p_script = Pattern.compile(regEx_script, Pattern.CASE_INSENSITIVE);
    Matcher m_script = p_script.matcher(htmlStr);
    htmlStr = m_script.replaceAll(""); //过滤script标签

    Pattern p_style = Pattern.compile(regEx_style, Pattern.CASE_INSENSITIVE);
    Matcher m_style = p_style.matcher(htmlStr);
    htmlStr = m_style.replaceAll(""); //过滤style标签

    Pattern p_html = Pattern.compile(regEx_html, Pattern.CASE_INSENSITIVE);
    Matcher m_html = p_html.matcher(htmlStr);
    htmlStr = m_html.replaceAll(""); //过滤html标签

    return htmlStr.trim(); //返回文本字符串
  }


}
