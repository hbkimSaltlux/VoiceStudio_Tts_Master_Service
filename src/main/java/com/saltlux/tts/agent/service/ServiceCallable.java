package com.saltlux.tts.agent.service;

import java.util.concurrent.Callable;

import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.saltlux.tts.agent.output.Result;
import com.saltlux.tts.util.CommonUtil;
import com.saltlux.tts.util.HttpUtil;

public class ServiceCallable implements Callable<Result> {
	private static final Logger logger = LoggerFactory.getLogger(ServiceCallable.class);
	private static final int RETRY = 3;
	private ServiceInput input;
	private CloseableHttpClient httpClient;

	public ServiceCallable(ServiceInput input, CloseableHttpClient httpClient) {
		this.input = input;
		this.httpClient = httpClient;
	}

	@Override
	public Result call() throws Exception {
		Result result = null;
		try {
			String voiceId = input.getVoiceId();
			String url = input.getUrl();
			String sentence = input.getText();
			int segment = input.getSegment();
			if (sentence == null) {
				throw new NullPointerException();
			}
			String encodedSentence = HttpUtil.urlEncode(sentence);

			String requestUrl = url + encodedSentence;

			byte[] wav = null;
			for (int cnt = 0; cnt < RETRY; cnt++) {
				try {
					long startTime = System.currentTimeMillis();
					wav = HttpUtil.doGetByte(httpClient, requestUrl);
					logger.info("http response time : {} ms", (System.currentTimeMillis() - startTime));
					break;
				} catch (ConnectTimeoutException e) {
					logger.info("voice id : {}\nmessage :", voiceId, CommonUtil.getExceptionMessage(e));
				}
			}

			if (wav == null) {
				throw new Exception("no response : " + url);
			}

			String encodedWav = CommonUtil.base64encoding(wav);
		
			ServiceOutput output = new ServiceOutput(segment, encodedWav);
			result = new Result(output);
		} catch (Exception e) {
			e.printStackTrace();
			result = new Result(input, -1, CommonUtil.getExceptionMessage(e));
		}
		return result;
	}

//	@Override
//	public Result call() throws Exception {
//		Result result = null;
//		try {
//			String voiceId = input.getVoiceId();
//			String url = input.getUrl();
//			String sentence = input.getText();
//			int segment = input.getSegment();
//			if (sentence == null) {
//				throw new NullPointerException();
//			}
////			logger.info("voiceId : {}\ttext : {}\turl : {}", voiceId, sentence, url);
//			String encodedSentence = HttpUtil.urlEncode(sentence);
//
//			String requestUrl = url + encodedSentence;
//
//			byte[] wav = null;
//			for (int cnt = 0; cnt < RETRY; cnt++) {
//				try {
//					long startTime = System.currentTimeMillis();
//					wav = HttpUtil.doGetByte(requestUrl);
//					logger.info("http response time : {} ms", (System.currentTimeMillis() - startTime));
//					break;
//				} catch (ConnectTimeoutException e) {
//					logger.info("voice id : {}\nmessage :", voiceId, CommonUtil.getExceptionMessage(e));
//				}
//			}
//
//			if (wav == null) {
//				throw new Exception("no response : " + url);
//			}
//
//			String encodedWav = CommonUtil.base64encoding(wav);
//			logger.info("encoded wav : {}", encodedWav);
//			ServiceOutput output = new ServiceOutput(segment, encodedWav);
//			result = new Result(output);
//		} catch (Exception e) {
//			result = new Result(input, -1, CommonUtil.getExceptionMessage(e));
//		}
//		return result;
//	}

}
