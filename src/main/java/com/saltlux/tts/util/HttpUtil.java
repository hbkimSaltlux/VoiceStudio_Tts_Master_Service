package com.saltlux.tts.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpUtil {
	private static final Logger logger = LoggerFactory.getLogger(HttpUtil.class);

	public static String doGetSimpleString(String uri) throws Exception {
		long startTime = System.currentTimeMillis();
		URL url = new URL(uri);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();

		conn.setRequestMethod("GET");
		conn.setRequestProperty("User-Agent", "Mozilla/5.0");

		int respCode = conn.getResponseCode();
		logger.info("http prepare time : {} ms", (System.currentTimeMillis() - startTime));

		StringBuffer response = new StringBuffer();

		startTime = System.currentTimeMillis();
		if (respCode == 200) {
			BufferedReader inStream = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

			String line = null;
			while ((line = inStream.readLine()) != null) {
				response.append(line);
			}

			inStream.close();
		}
		logger.info("http body read time : {} ms", (System.currentTimeMillis() - startTime));

		return response.toString();
	}

	public static String doGetResponseString(String uri) throws Exception {
		HttpClient client = HttpClientBuilder.create().build();

		HttpGet request = new HttpGet(uri);
		request.addHeader("Accept", "application/text");
		request.addHeader("Content-Type", "application/text");

		HttpResponse response = client.execute(request);

		HttpEntity entity = response.getEntity();

		int contentLen = (int) entity.getContentLength();

		char[] buff = new char[contentLen];

		long totalStartTime = System.currentTimeMillis();
		if (entity != null) {
			BufferedReader instream = new BufferedReader(new InputStreamReader(entity.getContent(), "UTF-8"));
			long startTime = System.currentTimeMillis();
			int len = instream.read(buff, 0, contentLen);
			logger.info("read len : {}\t content len : {}", len,contentLen);
			logger.info("instram read time : {} ms", (System.currentTimeMillis() - startTime));

			//			instream.close();
		}
		logger.info("http body read time : {} ms", (System.currentTimeMillis() - totalStartTime));
		return buff.toString();
	}

	public static byte[] doGetResponseByte(String uri) throws Exception {

		HttpClient client = HttpClientBuilder.create().build();

		HttpGet request = new HttpGet(uri);
		request.addHeader("Accept", "application/octet-stream");
		request.addHeader("Content-Type", "application/octet-stream");

		HttpResponse response = client.execute(request);

		HttpEntity entity = response.getEntity();
		int contentLen = (int) entity.getContentLength();
		logger.info("content len : {}", contentLen);
		ByteArrayOutputStream bout = null;
		long totalStartTime = System.currentTimeMillis();
		if (entity != null) {
			bout = new ByteArrayOutputStream();
			byte[] buff = new byte[contentLen];
			InputStream instream = entity.getContent();
			logger.info("instream available : {}", instream.available());
			int len = instream.read(buff);

			logger.info("buff len : {}\t{}", len,buff.length);
			bout.write(buff);
		} 
		logger.info("http body read time : {} ms", (System.currentTimeMillis() - totalStartTime));
		return bout.toByteArray();
	}

	public static String doGetString(String uri) throws Exception {

		HttpClient client = HttpClientBuilder.create().build();

		HttpGet request = new HttpGet(uri);
		request.addHeader("Accept", "application/text");
		request.addHeader("Content-Type", "application/text");

		HttpResponse response = client.execute(request);

		HttpEntity entity = response.getEntity();
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		if (entity != null) {
			InputStream instream = entity.getContent();
			byte[] buf = new byte[1024];
			int len = 0;
			while ((len = instream.read(buf, 0, 1023)) > -1) {
				bout.write(buf, 0, len);
			}
		}
		return new String(bout.toByteArray(), "UTF-8");
	}

	public static byte[] doGetByte(String uri) throws Exception {

		HttpClient client = HttpClientBuilder.create().build();

		HttpGet request = new HttpGet(uri);
		request.addHeader("Accept", "application/octet-stream");
		request.addHeader("Content-Type", "application/octet-stream");

		HttpResponse response = client.execute(request);

		HttpEntity entity = response.getEntity();
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		if (entity != null) {
			InputStream instream = entity.getContent();
			byte[] buf = new byte[1024];
			int len = 0;
			while (true) {
				len = instream.read(buf, 0, 1023);
				if (len == -1) {
					break;
				}
				bout.write(buf, 0, len);
			}
			byte[] result = bout.toByteArray();
			bout.close();
			return result;
		}
		return null;
	}

	public static byte[] doGetByte(CloseableHttpClient httpClient, String uri) throws Exception {

		HttpGet request = new HttpGet(uri);
		request.addHeader("Accept", "application/octet-stream");
		request.addHeader("Content-Type", "application/octet-stream");

		CloseableHttpResponse response = httpClient.execute(request);

		HttpEntity entity = response.getEntity();
		try(ByteArrayOutputStream bout = new ByteArrayOutputStream()){
			if (entity != null) {
				InputStream instream = entity.getContent();
				byte[] buf = new byte[1024];
				int len ;
				while ((len = instream.read(buf)) != -1) {
					bout.write(buf, 0, len);
				}
				return bout.toByteArray();
			}
		}
		return null;
	}

	public static String urlEncode(String text) throws UnsupportedEncodingException {
		return URLEncoder.encode(text, "UTF-8");
	}

	public static void main(String[] args) throws Exception {
		String url = "http://211.109.9.67:2819/eva/ttsstream?voice=1&message=11";
		byte[] data = doGetByte(url);
		WavMerger.writeWaveFile(data, "c:\\temp\\aa.wav");

	}
}
