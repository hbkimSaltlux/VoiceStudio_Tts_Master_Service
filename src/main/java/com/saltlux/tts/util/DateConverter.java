package com.saltlux.tts.util;

public abstract class DateConverter {
  private final String pattern;
  public DateConverter(String pattern) {
    this.pattern = pattern;
  }
  public String getPattern() {
    return pattern;
  }
  abstract String convert(String[] splist);
}
