package com.saltlux.tts.agent;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.filechooser.FileNameExtensionFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.saltlux.tts.agent.output.Result;
import com.saltlux.tts.agent.resource.input.ConnectEndpointInput;
import com.saltlux.tts.agent.resource.input.VoiceIdInput;
import com.saltlux.tts.agent.service.ServiceHelper;
import com.saltlux.tts.agent.service.input.ProcessRecoveryInput;
import com.saltlux.tts.agent.service.input.StreamInput;
import com.saltlux.tts.util.CommonUtil;

public class ServiceAgent {
	private static final Logger logger = LoggerFactory.getLogger(ServiceAgent.class);

	private Map<String, ServiceHelper> helperes = new ConcurrentHashMap<>(1);

	public void serviceConnect(ConnectEndpointInput input) {
		String voiceId = input.getVoiceId();
		Set<String> urls = input.getUrlSet();
		logger.info("service connect input : {}", input);

		ServiceHelper helper = new ServiceHelper(voiceId, urls);

		helperes.putIfAbsent(voiceId, helper);

		logger.info("connectors keyset : {}", helperes.keySet());
	}

	public Result getRequestService(StreamInput input) {
		String voiceId = input.getVoiceId();
		String text = input.getText();
		boolean useCache = input.isUseCache();
		boolean replace = input.isReplace();

		logger.info("connectors keyset : {}", helperes.keySet());

		ServiceHelper helper = helperes.getOrDefault(voiceId, null);

		if (helper == null) {
			return new Result(null, -1, "invalid voiceId");
		}

		Result result = null;

		if (!replace) {
			result = helper.getWavResult(text, useCache, replace);
		} else {
			result = helper.getWavResult(text, false, replace);
		}

//		if (useCache || replace) {
//			String hashedFilename = CommonUtil.hashMD5(text) + ".wav";
//
//			String cacheDirPath = CommonUtil.loadResources("cacheDir");
//
//			File cacheDir = new File(cacheDirPath);
//			File wavDir = new File(cacheDir, voiceId);
//
//			if (!wavDir.exists()) {
//				wavDir.mkdirs();
//			}
//
//			File saveFile = new File(wavDir, hashedFilename);
//
//			if (!saveFile.exists() || replace) {
//				logger.info("cache memory save");
//				String encodedWav = (String) result.getResult();
//				byte[] wavbyte = CommonUtil.base64decoding(encodedWav);
//
//				FileOutputStream fos;
//				try {
//					fos = new FileOutputStream(saveFile);
//					fos.write(wavbyte);
//					fos.flush();
//					fos.close();
//				} catch (Exception e) {
//					logger.error(CommonUtil.getExceptionMessage(e));
//				}
//			}
//		}

		return result;
	}

	public Result processRecovery(ProcessRecoveryInput input) {
		String voiceId = input.getVoiceId();

		ServiceHelper helper = helperes.getOrDefault(voiceId, null);

		if (helper == null) {
			return new Result(null, -1, "invalid voiceId");
		}

		String url = input.getUrl();
		boolean activate = input.isActivate();

		boolean recovery = helper.recoveryProcess(url, activate);

		Result result = null;

		if (recovery) {
			result = new Result(true);
		} else {
			result = new Result(false, -1, "invalid url");
		}

		return result;
	}

	public void serviceFree(VoiceIdInput voiceIdInput) {
		String voiceId = voiceIdInput.getVoiceId();
		helperes.remove(voiceId);
	}

	public void serviceRecovery(Map<String, Set<String>> endpointMap) {
		for (String voiceId : endpointMap.keySet()) {
			Set<String> urls = endpointMap.get(voiceId);
			ServiceHelper helper = new ServiceHelper(voiceId, urls);
			helperes.putIfAbsent(voiceId, helper);
			logger.info("{} service recovery", voiceId);
		}
	}
	
	public boolean hasService(String voiceId) {
		return helperes.containsKey(voiceId);
	}
	
	public void resetCache(String voiceId) {
		ServiceHelper helper = helperes.getOrDefault(voiceId, null);
		helper.clearCache();
	}

}
