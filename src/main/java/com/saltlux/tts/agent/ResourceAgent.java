package com.saltlux.tts.agent;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.saltlux.tts.agent.output.Result;
import com.saltlux.tts.agent.resource.input.ConnectEndpointInput;
import com.saltlux.tts.agent.resource.input.EndpointMapTypeToken;
import com.saltlux.tts.agent.resource.input.VoiceIdInput;
import com.saltlux.tts.agent.service.input.StreamInput;
import com.saltlux.tts.util.CommonUtil;

public class ResourceAgent {
	private static final Logger logger = LoggerFactory.getLogger(ResourceAgent.class);

	private static final String VOICE_ID_CONF = CommonUtil.loadResources("voiceIdConf");
	private static final Map<String, Set<String>> ENDPOINT_MAP = new ConcurrentHashMap<>();
	private static final Map<String, String> REPLACE_MAP = new HashMap<String, String>();

	public ResourceAgent() {
		loadJson();
		loadDict();
	};

	public static Map<String, Set<String>> getCopyEndpointMap() {
		Map<String, Set<String>> tmp = new HashMap<>();
		tmp.putAll(ENDPOINT_MAP);
		return tmp;
	}

	public Result connectEndpoint(ConnectEndpointInput connectEndpointInput) {

		if (connectEndpointInput == null || connectEndpointInput.getVoiceId() == null || connectEndpointInput.getUrlSet() == null || connectEndpointInput.getUrlSet().isEmpty()) {
			return new Result(false, -1, "입력이 올바르지 않음. key: voiceId, urlSet");
		}
		String key = connectEndpointInput.getVoiceId();
		Set<String> value = connectEndpointInput.getUrlSet();

		if (ENDPOINT_MAP.containsKey(key)) {
			return new Result(false, -1, "이미 사용 중인 Voice ID");
		}

		boolean isCompleted = putEndpoint(key, value);
		if (!isCompleted) {
			return new Result(false, -1, "EndpointMap 추가 실패");
		}
		return new Result(true);
	}

	public Result freeEndpoint(VoiceIdInput voiceIdInput) {
		if (voiceIdInput == null) {
			return new Result(false, -1, "입력이 올바르지 않음. key: voiceId");
		}

		String key = voiceIdInput.getVoiceId();
		if (!ENDPOINT_MAP.containsKey(key)) {
			return new Result(false, -1, "사용 중인 Voice ID가 아님");
		}

		boolean isCompleted = removeEndpoint(key);
		if (!isCompleted) {
			return new Result(false, -1, "EndpointMap 해제 실패");
		}
		return new Result(true);
	}

	private synchronized boolean putEndpoint(String key, Set<String> value) {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		Map<String, Set<String>> backup = getCopyEndpointMap();
		try {
			ENDPOINT_MAP.put(key, value);
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(VOICE_ID_CONF), "UTF-8"));
			writer.write(gson.toJson(ENDPOINT_MAP));
			writer.close();
			return true;
		} catch (IOException e) {
			ENDPOINT_MAP.clear();
			ENDPOINT_MAP.putAll(backup);
			logger.error(CommonUtil.getExceptionMessage(e));
		}
		return false;
	}

	private synchronized boolean removeEndpoint(String key) {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		Map<String, Set<String>> backup = getCopyEndpointMap();
		try {
			ENDPOINT_MAP.remove(key);
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(VOICE_ID_CONF), "UTF-8"));
			writer.write(gson.toJson(ENDPOINT_MAP));
			writer.close();
			return true;
		} catch (IOException e) {
			ENDPOINT_MAP.clear();
			ENDPOINT_MAP.putAll(backup);
			logger.error(CommonUtil.getExceptionMessage(e));
		}
		return false;
	}

	private void loadJson() {
		Gson gson = new Gson();

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(VOICE_ID_CONF), "UTF-8"));

			Map<String, Set<String>> tmp = gson.fromJson(reader, new EndpointMapTypeToken().getType());
			ENDPOINT_MAP.putAll(tmp);
		} catch (IOException e) {
			logger.error(CommonUtil.getExceptionMessage(e));
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				logger.error(CommonUtil.getExceptionMessage(e));
			}
		}
	}

	private void loadDict() {
		String dictPath = CommonUtil.loadResources("replaceDict");

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(dictPath), "UTF-8"));

			Map<String, String> tmp = new HashMap<String, String>();
			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] pair = line.split("\t");

				if (pair.length != 2) {
					continue;
				}

				String key = pair[0].trim();
				String value = pair[1].trim();

				key = CommonUtil.removeHtml(key);
				key = StringUtils.deleteWhitespace(key);

				tmp.put(key, value);
			}

			if (!tmp.isEmpty()) {
				REPLACE_MAP.putAll(tmp);
			}
		} catch (IOException e) {
			logger.error(CommonUtil.getExceptionMessage(e));
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				logger.error(CommonUtil.getExceptionMessage(e));
			}
		}
	}

//	@SuppressWarnings("unchecked")
//	private static String loadResources(String key) {
//		ClassPathResource resource = new ClassPathResource("resources.json");
//		
//		Gson gson = new Gson();
//		
//		Map<String, String> resourceMap = null;
//		try {
//			BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), "UTF-8"));
//			resourceMap = new HashMap<>();
//			resourceMap = gson.fromJson(reader, resourceMap.getClass());
//		} catch (Exception e) {
//			logger.error(CommonUtil.getExceptionMessage(e));
//		}
//		
//		if (resourceMap != null) {
//			return resourceMap.get(key);
//		} else {
//			return null;
//		}
//	}

	public synchronized boolean saveRequestText(String voiceId, String text) {
		try {
			String logDirPath = CommonUtil.loadResources("requestLogsDir");
			File logDir = new File(logDirPath);
			File logFile = new File(logDir, voiceId + ".out");
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logFile, true), "UTF-8"));
			writer.write(text + "'\n");
			writer.close();
			return true;
		} catch (IOException e) {
			logger.error(CommonUtil.getExceptionMessage(e));
		}
		return false;
	}

	public synchronized void saveCacheWave(Result result, StreamInput input) {
		String voiceId = input.getVoiceId();
		String text = input.getText();
		boolean useCache = input.isUseCache();
		boolean replace = input.isReplace();

		if (useCache || replace) {
			String hashedFilename = CommonUtil.hashMD5(text) + ".wav";

			String cacheDirPath = CommonUtil.loadResources("cacheDir");

			File cacheDir = new File(cacheDirPath);
			File wavDir = new File(cacheDir, voiceId);

			if (!wavDir.exists()) {
				wavDir.mkdirs();
			}

			File saveFile = new File(wavDir, hashedFilename);
			File writeFile = new File(wavDir, "cacheinfo.txt");

			if (!saveFile.exists() || replace) {
				logger.info("cache memory save");
				String encodedWav = (String) result.getResult();
				byte[] wavbyte = CommonUtil.base64decoding(encodedWav);
				boolean writeInfo = !saveFile.exists();

				FileOutputStream fos;
				BufferedWriter writer;
				try {
					fos = new FileOutputStream(saveFile);
					fos.write(wavbyte);
					fos.flush();
					fos.close();
					
					if (writeInfo) {
						writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(writeFile, true), "UTF-8"));
						writer.write(hashedFilename + "\t" + text + "\n");
						writer.flush();
						writer.close();
					}
					
				} catch (Exception e) {
					logger.error(CommonUtil.getExceptionMessage(e));
				}
			}
		}
	}
	
	public String replaceText(String text) {
		// html tag 제거, CES를 위한 기능 12/30 hbkim
		text = CommonUtil.removeHtml(text);
		String key = StringUtils.deleteWhitespace(text);

		if (REPLACE_MAP.containsKey(key)) {
			return REPLACE_MAP.get(key);
		} else {
			return text;
		}
	}
	
	public synchronized void removeCache(String voiceId) {
		String cacheDirPath = CommonUtil.loadResources("cacheDir");
		
		File cacheDir = new File(cacheDirPath);
		File wavDir = new File(cacheDir, voiceId);
		
		if (wavDir.exists()) {
			for (File cacheFile : wavDir.listFiles()) {
				cacheFile.delete();
			}
		} 
	}

	public static void main(String[] args) {
		System.out.println(VOICE_ID_CONF);
	}
}
