package com.saltlux.tts.agent.service;

import java.util.Random;
import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.saltlux.tts.util.CommonUtil;
import com.saltlux.tts.util.HttpUtil;

public class ServiceSemaphore {
	private static final Logger logger = LoggerFactory.getLogger(ServiceSemaphore.class);
	private Semaphore resources;
	private int maxResource;

	public ServiceSemaphore(int maxResource) {
		this.maxResource = maxResource;
		this.resources = new Semaphore(maxResource);
	}

	public String useResource(String url) {
		String result = null;
		try {
			resources.acquire();
			
			logger.info("acquire url : {}", url);
			
			Thread.sleep(1000 * (new Random().nextInt(3)+1));
			
//			byte[] wav = HttpUtil.doGetByte(url);
//			result = CommonUtil.base64encoding(wav);
		} catch (Exception e) {
			resources.release();
		} finally {
			resources.release();
		}
		return result;
	}
}
