package com.saltlux.tts.agent.service;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.saltlux.tts.agent.output.Result;
import com.saltlux.tts.util.CommonUtil;
import com.saltlux.tts.util.SimpleEntry;
import com.saltlux.tts.util.WavMerger;

public class ServiceHelper {
	private static final Logger logger = LoggerFactory.getLogger(ServiceHelper.class);

	private String voiceId;
	private List<ProcessUrl> urls;
	private int urlsLen;
	private AtomicInteger syncInteger = new AtomicInteger();

	private static final long MAXIMUM_CACHE_SIZE = 500;
	private static final int MAXIMUM_REQUEST_SIZE = 500;

	private LoadingCache<String, String> wavCache;

	private CloseableHttpClient closeableHttpClient;

	public ServiceHelper(String voiceId, Set<String> urls) {
		this.voiceId = voiceId;
		this.urls = new ArrayList<ProcessUrl>();
		urls.stream().forEach(url -> this.urls.add(new ProcessUrl(url)));
		this.urlsLen = urls.size();

		initCache();

		PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
		connectionManager.setMaxTotal(MAXIMUM_REQUEST_SIZE);
		connectionManager.setDefaultMaxPerRoute(urlsLen * 3 * 5);
		this.closeableHttpClient = HttpClients.custom().setConnectionManager(connectionManager).build();

	}

	public void initCache() {
		CacheLoader<String, String> wavCacheLoader = new CacheLoader<String, String>() {
			@Override
			public String load(String key) throws Exception {
				String encodedWav = null;

				String wavFileName = CommonUtil.hashMD5(key) + ".wav";

				String cacheDirPath = CommonUtil.loadResources("cacheDir");

				File cacheDir = new File(cacheDirPath);
				File wavDir = new File(cacheDir, voiceId);

				if (!wavDir.exists()) {
					wavDir.mkdirs();
				}

				File wavFile = new File(wavDir, wavFileName);

				if (wavFile.exists()) {
					logger.info("loading saved cache wav file");

					FileInputStream fis = new FileInputStream(wavFile);
					BufferedInputStream bis = new BufferedInputStream(fis);

					ByteArrayOutputStream buff = new ByteArrayOutputStream();

					byte[] bytebuff = new byte[1024 * 8];
					int readLen = 0;
					while ((readLen = bis.read(bytebuff)) != -1) {
						buff.write(bytebuff, 0, readLen);
					}

					byte[] wavByte = buff.toByteArray();

					bis.close();

					encodedWav = CommonUtil.base64encoding(wavByte);
				} else {
					encodedWav = getWav(key);
				}

				return encodedWav;
			}
		};

		wavCache = CacheBuilder.newBuilder().concurrencyLevel(urls.size())// .expireAfterAccess(7, TimeUnit.DAYS)
				.maximumSize(MAXIMUM_CACHE_SIZE).recordStats().build(wavCacheLoader);

		String cacheDirPath = CommonUtil.loadResources("cacheDir");

		File cacheDir = new File(cacheDirPath);
		File wavDir = new File(cacheDir, voiceId);

		if (!wavDir.exists()) {
			wavDir.mkdirs();
		}

	}

	public LoadingCache<String, String> getLoadingCache() {
		return wavCache;
	}

	public static void main(String[] args) {
		Set<String> urls = new LinkedHashSet<>();
		urls.add("http://211.109.9.128:52002/ss?text=");

		ServiceHelper helper = new ServiceHelper("sm2", urls);

		// String[] text = new String[] { "안녕하세요, 김성만입니다.", "안녕하세요, 김한빈입니다.", "안녕하세요,
		// 김재은입니다." };
		// /*Thread thread = new Thread(new Runnable() {
		//
		// @Override
		// public void run() {
		// // TODO Auto-generated method stub
		//
		// }
		// })*/
		// for (int i = 0; i < 2; ++i) {
		// for (String t : text) {
		// helper.getWavResult(t);
		// }
		// }
		String[] sp = helper.sentenceSpliter("How about tell hanbin? Adam? Eric?");
		for (String string : sp) {
			System.out.println(string);
		}

	}

	@Override
	public String toString() {
		return "ServiceHelper [voiceId=" + voiceId + ", urls=" + urls + "]";
	}

	private String[] sentenceSpliter(String text) {
		String[] dotTokens = splitTokens(new String[] { text }, "\\. ");
		String[] exclamtionTokens = splitTokens(dotTokens, "!");
		String[] questionTokens = splitTokens(exclamtionTokens, "\\?");
		String[] splitSentence = mergeTokens(questionTokens, 3);
		return splitSentence;
	}

	private String[] mergeTokens(String[] tokens, int minWord) {
		List<String> mergeSentences = new ArrayList<String>();
		StringBuilder tmpStr = new StringBuilder();

		for (String token : tokens) {
			if (token.split(" ").length < minWord) {
				tmpStr.append(token).append(" ");
			} else {
				if (mergeSentences.isEmpty()) {
					String mergeSentence = tmpStr.length() > 0 ? tmpStr.toString() + token : token;
					mergeSentences.add(mergeSentence);
					tmpStr.setLength(0);
				} else if (tmpStr.length() > 0) {
					int lastIndex = mergeSentences.size() - 1;
					String mergeSentence = mergeSentences.get(lastIndex) + " " + tmpStr.toString().trim();
					mergeSentences.set(lastIndex, mergeSentence);
					mergeSentences.add(token);
					tmpStr.setLength(0);
				} else {
					mergeSentences.add(token);
				}

			}
		}

		if (tmpStr.length() > 0) {
			int lastIndex = mergeSentences.size() - 1;
			String mergeSentence = tmpStr.toString().trim();
			if (lastIndex > -1) {
				mergeSentence = mergeSentences.get(lastIndex) + " " + mergeSentence;
				mergeSentences.set(lastIndex, mergeSentence);
			} else {
				mergeSentences.add(mergeSentence);
			}
		}

		return mergeSentences.toArray(new String[mergeSentences.size()]);
	}

	private String[] splitTokens(String[] tokens, String delimiter) {
		List<String> result = new ArrayList<String>();
		for (String token : tokens) {
			String[] sentences = token.split(delimiter);
			for (String sentence : sentences) {
				sentence = sentence.trim();
				if (sentence.length() != 0) {
					result.add(sentence);
				}
			}
		}
		String[] resultArray = result.toArray(new String[result.size()]);
		delimiter = delimiter.replace("\\", "").trim();
		return concatTokens(resultArray, delimiter);
	}

	private String[] concatTokens(String[] tokens, String delimiter) {
		for (int i = 0; i < tokens.length; i++) {
			String token = tokens[i];
			token = token.trim();
			if (delimiter.equals(".")) {
				if (!token.endsWith("?") && !token.endsWith("!") && !token.endsWith(".")) {
					token += delimiter;
				}
			} else if (delimiter.equals("?")) {
				if (!token.endsWith(".") && !token.endsWith("!")) {
					token += delimiter;
				}
			} else if (delimiter.equals("!")) {
				if (!token.endsWith(".") && !token.endsWith("?")) {
					token += delimiter;
				}
			}

			tokens[i] = token;
		}

		return tokens;
	}

	private String keyGenerate(int segment, String sentence) {
		HashCodeBuilder builder = new HashCodeBuilder(17, 37);
		builder.append(System.currentTimeMillis()).append(segment).append(sentence);
		int hashCode = builder.toHashCode();
		return String.valueOf(hashCode);
	}

	public String getWav(String text) throws Exception {
		String[] sentences = sentenceSpliter(text);
		int poolSize = sentences.length;

		ExecutorService executor = ExecutionHelper.CreateExcutor(poolSize, voiceId);

		try {
			final ExecutionHelper<Result> runner = new ExecutionHelper<Result>(executor);

			for (int segment = 0; segment < poolSize; segment++) {
				String sentence = sentences[segment];
				String key = keyGenerate(segment, sentence);
				String url = getServiceUrl();

				ServiceInput input = new ServiceInput(voiceId, url, key, segment, sentence);
				ServiceCallable callable = new ServiceCallable(input, closeableHttpClient);

				runner.submit(callable);
			}

			Map<Integer, String> wavMap = new TreeMap<>();

			for (Result callResult : runner) {
				if (callResult.getErrorCode() == -1) {
					ServiceInput serviceInput = (ServiceInput) callResult.getResult();
					ProcessUrl errorProcess = urls.get(urls.indexOf(new ProcessUrl(serviceInput.getUrl())));
					errorProcess.setActivate(false);
					serviceInput.setUrl(getServiceUrl());

					ServiceCallable callable = new ServiceCallable(serviceInput, closeableHttpClient);
					runner.submit(callable);
					logger.info(callResult.getErrorMessage());
				} else {
					ServiceOutput serviceOutput = (ServiceOutput) callResult.getResult();
					int segment = serviceOutput.getSegment();
					String encodedWav = serviceOutput.getEncodedWav();
					wavMap.put(segment, encodedWav);
				}
			}

			List<SimpleEntry<byte[], Integer>> wavEntry = new LinkedList<>();

			for (int segment : wavMap.keySet()) {
				String encodedWav = wavMap.get(segment);
				byte[] wavBytes = CommonUtil.base64decoding(encodedWav);

				SimpleEntry<byte[], Integer> entry = new SimpleEntry<byte[], Integer>(wavBytes, segment);
				wavEntry.add(entry);
			}

			byte[] mergedWavBytes = WavMerger.merge(wavEntry);
			String mergeWav = CommonUtil.base64encoding(mergedWavBytes);
			return mergeWav;
		} finally {
			ExecutionHelper.shutdownExecutorService(executor);
		}
	}

	public Result getWavResult(String text, boolean useCache, boolean replace) {
		logger.info("voiceId : {}\ttext : {}", voiceId, text);
		long totalStartTime = System.currentTimeMillis();

		Result result = null;
		try {
			String wav = null;
			if (useCache) {
				wav = wavCache.get(text);
				logger.info(wavCache.stats().toString());
			} else if (replace) {
				wav = getWav(text);
				wavCache.put(text, wav);
				logger.info("cache is replaced\tcache key : {}", text);
			} else {
				wav = getWav(text);
			}
			result = new Result(wav);
			logger.info("Total execute time : {} ms", (System.currentTimeMillis() - totalStartTime));
		} catch (Exception e) {
			e.printStackTrace();
			result = new Result(null, -1, CommonUtil.getExceptionMessage(e));
		}
		return result;
	}

	private String getServiceUrl() {
		return getUrl();
	}

	private String getUrl() {
		int order = getOrder();
		ProcessUrl processUrl = urls.get(order);
		if (!processUrl.isActivate()) {
			return getUrl();
		}
		return processUrl.getUrl();
	}

	private int getOrder() {
		int order = syncInteger.getAndIncrement();
		return order % urlsLen;
	}

	public boolean recoveryProcess(String url, boolean activate) {
		try {
			ProcessUrl process = urls.get(urls.indexOf(new ProcessUrl(url)));
			process.setActivate(activate);
			return true;
		} catch (Exception e) {
			logger.info(CommonUtil.getExceptionMessage(e));
			return false;
		}
	}

	public synchronized void clearCache() {
		wavCache.invalidateAll();
		wavCache.cleanUp();
	}
}
