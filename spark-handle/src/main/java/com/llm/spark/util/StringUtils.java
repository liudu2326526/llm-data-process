package com.llm.spark.util;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Liu Du
 * @Date 2024/12/18
 */
public class StringUtils extends org.apache.commons.lang3.StringUtils {


  public static String join(final List<?> list, final String separator, Integer length) {
    if (list == null) {
      return null;
    }
    if (list.size() >= length) {
      return join(list.subList(0, length).iterator(), separator);
    } else {
      return join(list.iterator(), separator);
    }

  }

  public static String lineToHump(String str) {
    Pattern pattern = Pattern.compile("[_.-](\\w)");
    Matcher matcher = pattern.matcher(str);
    StringBuffer sb = new StringBuffer(str);
    if (matcher.find()) {
      sb = new StringBuffer();
      matcher.appendReplacement(sb, matcher.group(1).toUpperCase());
      matcher.appendTail(sb);
    } else {
      return sb.toString();
    }
    return lineToHump(sb.toString());
  }


}
