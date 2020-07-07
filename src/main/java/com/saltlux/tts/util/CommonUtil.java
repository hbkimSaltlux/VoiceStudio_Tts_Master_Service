package com.saltlux.tts.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import com.google.gson.Gson;

public class CommonUtil {
	private static final Logger logger = LoggerFactory.getLogger(CommonUtil.class);

	public static String base64encoding(byte[] bts) {
		return Base64.getEncoder().encodeToString(bts);
	}

	public static byte[] base64decoding(String encodedStr) {
		return Base64.getDecoder().decode(encodedStr);
	}

	public static String getExceptionMessage(Exception e) {
		StackTraceElement[] ste = e.getStackTrace();

		String className = ste[0].getClassName();
		String methodName = ste[0].getMethodName();
		String fileName = ste[0].getFileName();
		int lineNumber = ste[0].getLineNumber();

		StringBuilder sb = new StringBuilder();
		sb.append("Exception: ").append(e.getMessage()).append("\n\tat ").append(className + ".").append(methodName + "(").append(fileName + ":").append(lineNumber + ")");
		return sb.toString();
	}

	public static String loadResources(String key) {
		ClassPathResource resource = new ClassPathResource("resources.json");

		Gson gson = new Gson();

		Map<String, String> resourceMap = null;
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), "UTF-8"));
			resourceMap = new HashMap<>();
			resourceMap = gson.fromJson(reader, resourceMap.getClass());
		} catch (Exception e) {
			logger.error(getExceptionMessage(e));
		}

		if (resourceMap != null) {
			return resourceMap.get(key);
		} else {
			return null;
		}
	}

	public static String hashMD5(String text) {
		String md5 = null;

		try {
			MessageDigest hashing = MessageDigest.getInstance("MD5");
			hashing.update(text.getBytes());
			byte[] bData = hashing.digest();

			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < bData.length; i++) {
				sb.append(Integer.toString((bData[i] & 0xff) + 0x100, 16).substring(1));
			}
			
			md5 = sb.toString();
		} catch (NoSuchAlgorithmException e) {
			logger.error(getExceptionMessage(e));
		}
		
		return md5;
	}
	
	public static String removeHtml(String text) {
		text = text.replaceAll("<(/)?([a-zA-Z]*)(\\s[a-zA-Z]*=[^>]*)?(\\s)*(/)?>", " ").trim();
		return String.join(" ", StringUtils.split(text));
	}

	public static void main(String[] args) throws UnsupportedEncodingException {
		String text = "Oh are you sleepy? You look like sleepy Joe, LOL.";
		System.out.println(hashMD5(text));
	}
}
