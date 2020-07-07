package com.saltlux.tts.util;

import com.saltlux.tts.util.TrieSearcher.Result;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class ModifyTextForTts {

  private ModifyTextForTts() {
    
  }
  //2018/12/06(LST) <서울특별시>  날씨:구름조금  기온:7.0℃   강수량(강우량):눈/비 없음  습도:35%  풍량:바람이 약간 강함
  //음성합성에 도움이 되지 않는 특수문자 제거
  //숫자를 발음으로 변경
  //날짜의 경우 숫자를 발음으로 변경하고 년/월/일 붙이기
  //시간의 경우 몇시 몇분으로 명시
  //다양한 단위를 사전을 사용하여 발음으로 변경
  //영어의 경우 사전을 사용하여 발음으로 변경

//* 단위: km, m,  m² , 분, 시간, 원, 위(1위,2위...), %, 점(평점 8.53점), 세(15세 관람가) , YYYY년, MM월, DD일, YYYY.MM.DD(2018.11.25)

//* 영어: CEO, Tony, EVA, AI  
  public static String modify(String message) {
    String replaceStr = removeBracket(message);
    replaceStr = remove(replaceStr);
    replaceStr = convertDateTimeOrNumber(replaceStr);
    replaceStr = replace(replaceStr);
    return replaceStr;
  }
  
  private static String convertDateTimeOrNumber(String message) {
    ConvertDic convert = ConvertDic.getinstance();
    StringBuilder buf = new StringBuilder();
    String[] splist = message.split(" ");
    for (String sp : splist) {
      String num = convert.convertDate(sp);
      String tnum = convertPhoneNum(num);
      if (num.equals(tnum)) {
        num = convertNumber(num);
      } else {
        num = tnum;
      }
      buf.append(num).append(' ');
    }
    return buf.toString().trim();
  }
  
  public static String convertNumber(String term) {
    
    int numcnt = 0;
    
    if (term.length() == 1 && term.charAt(0) == ',') {
      return "";
    }
    
    for (int i = 0 ; i < term.length(); i++) {
      char ch = term.charAt(i);
      if (ch == ',' || (ch >= '0' && ch <= '9')) {
        numcnt++;
      }
    }
    if (numcnt == 0) {
      return term;
    }
    
    int start = 0;
    int offset = 0;
    boolean isNum = false;
    StringBuilder buf = new StringBuilder();
    
    Character ch = null;
    for (int i = 0 ; i < term.length(); i++) {
      ch = term.charAt(i);
      if (ch == ',' || (ch >= '0' && ch <= '9')) {
        offset = i;
        isNum = true;
      } else {
        if (isNum) {
          buf.append(convertNum(term.substring(start, offset + 1)));
        }
        if (ch == '.') {
          if (i + 1 < term.length()) {
            char nch = term.charAt(i+1);
            if (nch >= '0' && nch <= '9') {
              buf.append("쩜");
            } else {
              buf.append(ch);
            }
          }
        } else {
          buf.append(ch);
        }
        start = i + 1;
        isNum = false;
      }
    }
    
    if (isNum) {
      buf.append(convertNum(term.substring(start, offset + 1)));
    }
    
    return buf.toString().trim();
  }
  
  private static String replace(String message) {
    ConvertDic dicObj = ConvertDic.getinstance();
    String replaceStr = message.toLowerCase();
    List<Result> reslist = dicObj.searcher.search(replaceStr);
    if (!reslist.isEmpty()) {
      for (Entry<String, String> conv : dicObj.convLowMap.entrySet()) {
        replaceStr = replaceStr.replace(conv.getKey(), conv.getValue());
      }
    }
    return replaceStr;
  }
  
  private static String removeBracket(String message) {
    StringBuilder buf = new StringBuilder();
    boolean inner = false;
    for (int i = 0 ; i < message.length(); i++) {
      char ch = message.charAt(i);
      if (ch == '(') {
        inner = true;
      } else if (ch == ')') {
        inner = false;
      } else {
        if (!inner) {
          buf.append(ch);
        }
      }
    }
    
    return buf.toString();
  }
  
  private static final Set<Character> removeSet = new HashSet<>();
  static {
    removeSet.add('{');
    removeSet.add('}');
    removeSet.add('[');
    removeSet.add(']');
    removeSet.add('<');
    removeSet.add('>');
    removeSet.add('"');
    removeSet.add('\'');
    removeSet.add('@');
    removeSet.add('$');
    removeSet.add('^');
    removeSet.add('\\');
  }
  
  private static String remove(String message) {
    StringBuilder buf = new StringBuilder();
    for (int i = 0 ; i < message.length(); i++) {
      char ch = message.charAt(i);
      if(!removeSet.contains(ch)) {
        buf.append(ch);
      }
    }
    
    return buf.toString();
  }
  
  private static final String[] numtohan = {"", "일", "이", "삼", "사", "오", "육", "칠", "팔", "구"};
  private static final String[] danwee = {"천", "", "십", "백"};
  private static final String[] dan = {"", "만", "억", "조", "경", "해", "자", "양", "구", "간", "정", "재", "극", "항하사", "아승기", "나유타", "불가사의", "무량대수"};
  
  private static final Map<Character, String> numtoHanMap = new HashMap<>();
  static {
    numtoHanMap.put('0', "공");
    numtoHanMap.put('1', "일");
    numtoHanMap.put('2', "이");
    numtoHanMap.put('3', "삼");
    numtoHanMap.put('4', "사");
    numtoHanMap.put('5', "오");
    numtoHanMap.put('6', "육");
    numtoHanMap.put('7', "칠");
    numtoHanMap.put('8', "팔");
    numtoHanMap.put('9', "구");
  }
  public static String convertPhoneNum(String source) {
    int len = 0;
    boolean isD = false;
    for (int i = 0 ; i < source.length(); i++) {
      char ch = source.charAt(i);
      if (ch == '-') {
        isD = true;
      }
      if (ch == '-' || (ch >= '0' && ch <= '9')) {
        len++;
      }
    }
    if (len != source.length() || isD == false) {
      return source;
    }
    
   StringBuilder buf = new StringBuilder();
   for (int i = 0 ; i < source.length(); i++) {
     char ch = source.charAt(i);
     if (ch == '-') {
       buf.append(" 다시 ");
     } else if (ch >= '0' && ch <= '9') {
       String nStr = numtoHanMap.get(ch);
       buf.append(nStr);
     }
   }
   return buf.toString();
  }
  public static String convertNum(String source) {
    
    if (source.length() > 72) {
      return source;
    }
    
    if (source.length() == 1 && source.charAt(0) == '0') {
      return "영";
    }
    
    try {
      
      StringBuilder numStr = new StringBuilder();
      int isZero = 1;
      int firstFlag = 1;
      int allZero = 1; //숫자가 모두 영일  경우 처리
      
      for (int j = 0, i = source.length(); i > 0 ; i--, j++) {
        if (source.charAt(j) == ',') {
          continue;
        }
        int currNum = Integer.parseInt(source.substring(j, j+1));
        StringBuilder danstr = new StringBuilder(); //현재 단위를 나타낼 문자열 초기화 
        if (currNum != 0) {
          danstr.append(danwee[i % 4]);
          isZero = 0;
          allZero = 0;
        }
        //만억조경과 같은 단위를 붙여준다.
        if(((i % 4) == 1) && ( isZero == 0 || firstFlag == 1 )  && allZero == 0) {
          danstr.append(dan[(int)(i/4)]).append(' ');
          isZero = 1;
          firstFlag = 0;
        }
        numStr.append(numtohan[currNum]).append(danstr);
      }
      String finalString = numStr.toString().trim();
      boolean isLastOne = false;
      if (finalString.charAt(finalString.length() - 1) == '일') {
        isLastOne = true;
      }
      finalString = finalString.replace("일", "");
      if (isLastOne) {
        finalString = finalString + "일";
      }
      
      return finalString;
    } catch (Exception e) {
      return source;
    }
  }
  
  public static void main(String[] args) {
    System.out.println(modify("1.9Km."));
    
  }
}
