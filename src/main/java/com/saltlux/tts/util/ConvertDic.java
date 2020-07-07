package com.saltlux.tts.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConvertDic {

  private static ConvertDic instance;
  public final Map<String, String> convMap = new HashMap<>();
  public final Map<String, String> convLowMap = new HashMap<>();
  public final TrieSearcher searcher = new TrieSearcher();

  public static synchronized ConvertDic getinstance() {
    if (instance == null) {
      instance = new ConvertDic();
    }
    return instance;
  }
  
  private ConvertDic() {
    init();
    initDateTime();
  }
  
  private void init() {
    convMap.put("CEO", "씨이오");
    convMap.put("Tony", "토니");
    convMap.put("EVA", "에바");
    convMap.put("AI", "에이아이");
    
    convMap.put("km", "키로미터");
    convMap.put("m", "미터");
    convMap.put("m²", "제곱미터");
    convMap.put("℃", "도씨. ");
    convMap.put("%", "퍼센트. ");
    convMap.put("#", "샾");
    convMap.put(":", " ");
    
    for (Entry<String, String> entry : convMap.entrySet()) {
      convLowMap.put(entry.getKey().toLowerCase(), entry.getValue());
      searcher.insq(entry.getKey().toLowerCase(), entry.getValue());
    }
  }
  
  public static class DateInfo {
    public String format;
    public String split;
    public String regEx;
    Pattern pattern;
  }
  private final List<DateInfo> dateInfoList = new ArrayList<>();
  private final Map<String, DateConverter> dateConvMap = new HashMap<>();
  
  public String convertDate(String messsage) {
    DateInfo findInfo = null;
    String matchString = null;
    int max = -1;
    for (DateInfo info : dateInfoList) {
      Matcher matcher = info.pattern.matcher(messsage);
      while(matcher.find()) {
        int len = matcher.end() - matcher.start();
        if (len > max) {
          max = len;
          findInfo = info;
          matchString = matcher.group(0);
        }
        break;
      }
    }
    if (findInfo != null) {
      DateConverter converter = dateConvMap.get(findInfo.format);
      if (converter != null) {
        String newMessage = converter.convert(matchString.split(findInfo.split));
        return newMessage;
      }
    }
    return messsage;
  }
  
  private void initDateTime() {
    try {
      java.io.InputStream rcInputStream = ConvertDic.class.getResourceAsStream("DateRegex.tbl");
      BufferedReader reader = new BufferedReader(new InputStreamReader(rcInputStream));
      String line = null;
      while((line = reader.readLine()) != null) {
        if (line.length() == 0) {
          continue;
        }
        String[] sp = line.split("\t");
        if (sp.length != 3) {
          System.out.println(line);
          continue;
        }
        DateInfo info = new DateInfo();
        info.format = sp[0];
        info.split = sp[1];
        info.regEx = sp[2];
        info.pattern = Pattern.compile(info.regEx);
        dateInfoList.add(info);
      }
      reader.close();
      
      DateConverter converter = new DateConverter("YYYY-MM-DD") {
        @Override
        String convert(String[] sp) {
          return sp[0] + "년" + sp[1] + "월" + sp[2] + "일";
        }
      };
      dateConvMap.put("YYYY-MM-DD", converter);
      
      converter = new DateConverter("MM-DD") {
        @Override
        String convert(String[] sp) {
          return sp[0] + "월" + sp[1] + "일";
        }
      };
      dateConvMap.put("MM-DD", converter);
      
      converter = new DateConverter("YYYY-MM") {
        @Override
        String convert(String[] sp) {
          return sp[0] + "년" + sp[1] + "월";
        }
      };
      dateConvMap.put("YYYY-MM", converter);
      
      converter = new DateConverter("YYYY") {
        @Override
        String convert(String[] sp) {
          return sp[0] + "년";
        }
      };
      dateConvMap.put("YYYY", converter);
      
      converter = new DateConverter("DD") {
        @Override
        String convert(String[] sp) {
          return sp[0] + "일";
        }
      };
      dateConvMap.put("DD", converter);
      
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  public static void main(String[] args) {
    ConvertDic runner = ConvertDic.getinstance();
    runner.convertDate("2일");
    
  }
}
