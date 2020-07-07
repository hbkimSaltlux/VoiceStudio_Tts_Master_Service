package com.saltlux.tts;

import org.apache.commons.lang3.StringUtils;

public class CodeTest {

	public static void main(String[] args) {
		String text = "hello my name is hanbin kim\ndo you want to eat\r\nnice to meet you.";
		
		System.out.println(text);
		System.out.println();
		System.out.println(StringUtils.deleteWhitespace(text));
	}

}
