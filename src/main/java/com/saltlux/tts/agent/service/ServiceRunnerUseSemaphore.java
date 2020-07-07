package com.saltlux.tts.agent.service;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import com.saltlux.tts.util.CommonUtil;
import com.saltlux.tts.util.HttpUtil;

public class ServiceRunnerUseSemaphore extends Thread {
	private String url;
	private ServiceInput input;
	private ServiceSemaphore connector;
	private ConcurrentHashMap<ServiceInput, String> responseMap;

	@Override
	public String toString() {
		return "ServiceRunner [url=" + url + ", input=" + input + ", connector=" + connector + ", responseMap=" + responseMap + "]";
	}

	public ServiceRunnerUseSemaphore(String url, ServiceInput input, ServiceSemaphore connector, ConcurrentHashMap<ServiceInput, String> responseMap) {
		this.url = url;
		this.input = input;
		this.connector = connector;
		this.responseMap = responseMap;
	}

	@Override
	public void run() {
		try {
			String text = input.getText();
			text = HttpUtil.urlEncode(text);

			String fullUrl = this.url + text;

			String encodedWav = connector.useResource(fullUrl);

			if (encodedWav != null) {
				responseMap.putIfAbsent(input, encodedWav);
			} else {
				responseMap.putIfAbsent(input, "ERR");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
