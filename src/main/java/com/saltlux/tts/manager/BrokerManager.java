package com.saltlux.tts.manager;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.saltlux.tts.agent.Agent;
import com.saltlux.tts.agent.IAgent;
import com.saltlux.tts.agent.ResourceAgent;
import com.saltlux.tts.agent.ServiceAgent;
import com.saltlux.tts.agent.output.Result;
import com.saltlux.tts.agent.service.input.StreamInput;
import com.saltlux.tts.function.FunctionEnum;
import com.saltlux.tts.util.CommonUtil;

import diotts.Pttsnet;
import ws.schild.jave.AudioAttributes;
import ws.schild.jave.Encoder;
import ws.schild.jave.EncodingAttributes;
import ws.schild.jave.MultimediaObject;

public class BrokerManager {
	private static BrokerManager manager = new BrokerManager();
	private static final Logger logger = LoggerFactory.getLogger(BrokerManager.class);

	// for selvy tts stream
	private static final AtomicLong counter = new AtomicLong();

	private static IAgent agent;

	private BrokerManager() {
	}

	static {
		agent = new Agent(new ResourceAgent(), new ServiceAgent());
	}

	public static synchronized BrokerManager getInstance() {
		return manager;
	}

	/**
	 * @param function : 수행할 기능
	 * @param input    : 입력 JSON
	 * @return
	 */
	public String getResult(FunctionEnum function, String input) {
		Result result = agent.getResult(function, input);
		Gson gson = new GsonBuilder().serializeNulls().create();
		return gson.toJson(result);
	}
	
	public String getTtsJson(FunctionEnum function, String input) {
		Result result = agent.getResult(function, input);
		Gson gson = new GsonBuilder().serializeNulls().create();
		return gson.toJson(result);
	}
	
	public String getTtsJsonMp3(FunctionEnum function, String input) {
		Gson gson = new Gson();
		StreamInput streamInput = gson.fromJson(input, StreamInput.class);

		String voiceId = streamInput.getVoiceId();
		String text = streamInput.getText();

		String saveDirPath = CommonUtil.loadResources("wavToMp3Dir");
		String wavDirPath = String.format("%s/%s/%s", saveDirPath, "wav", voiceId);
		String mp3DirPath = String.format("%s/%s/%s", saveDirPath, "mp3", voiceId);

		File wavDir = new File(wavDirPath);
		File mp3Dir = new File(mp3DirPath);

		if (!wavDir.exists()) {
			wavDir.mkdirs();
		}

		if (!mp3Dir.exists()) {
			mp3Dir.mkdirs();
		}

		String hashedFilename = CommonUtil.hashMD5(text);

		File wavFile = new File(wavDir, hashedFilename + ".wav");
		File mp3File = new File(mp3Dir, hashedFilename + ".mp3");

		Result result = agent.getResult(function, input);
		byte[] mp3 = null;
		if (result.getResult() != null && result.getResult() instanceof String) {
			String encodedWav = (String) result.getResult();
			byte[] wavbyte = CommonUtil.base64decoding(encodedWav);

			FileOutputStream fos;
			try {
				fos = new FileOutputStream(wavFile);
				fos.write(wavbyte);
				fos.flush();
				fos.close();

				AudioAttributes audio = new AudioAttributes();
				audio.setCodec("libmp3lame");
				audio.setBitRate(320000);
				audio.setChannels(1);
				audio.setSamplingRate(22050);

				EncodingAttributes attrs = new EncodingAttributes();
				attrs.setFormat("mp3");
				attrs.setAudioAttributes(audio);

				Encoder wavToMp3 = new Encoder();

				wavToMp3.encode(new MultimediaObject(wavFile), mp3File, attrs);

				FileInputStream fis = new FileInputStream(mp3File);
				BufferedInputStream bis = new BufferedInputStream(fis);

				ByteArrayOutputStream buff = new ByteArrayOutputStream();

				byte[] bytebuff = new byte[1024 * 8];
				int readLen = 0;
				while ((readLen = bis.read(bytebuff)) != -1) {
					buff.write(bytebuff, 0, readLen);
				}

				mp3 = buff.toByteArray();

				bis.close();
			} catch (Exception e) {
				logger.error(CommonUtil.getExceptionMessage(e));
			}

		}
		
		String encodedMp3 = CommonUtil.base64encoding(mp3);
		Result encodedResult = new Result(encodedMp3, result.getErrorCode(), result.getErrorMessage());
		Gson gson2 = new GsonBuilder().serializeNulls().create();
		return gson2.toJson(encodedResult);
	}


	public byte[] getTtsStream(FunctionEnum function, String input) {
		Result result = agent.getResult(function, input);
		byte[] wav = null;
		if (result.getResult() != null && result.getResult() instanceof String) {
			String encodedWav = (String) result.getResult();
			wav = CommonUtil.base64decoding(encodedWav);
		}
		
		return wav;
	}

	public byte[] getTtsStreamMp3(FunctionEnum function, String input) {
		Gson gson = new Gson();
		StreamInput streamInput = gson.fromJson(input, StreamInput.class);

		String voiceId = streamInput.getVoiceId();
		String text = streamInput.getText();

		String saveDirPath = CommonUtil.loadResources("wavToMp3Dir");
		String wavDirPath = String.format("%s/%s/%s", saveDirPath, "wav", voiceId);
		String mp3DirPath = String.format("%s/%s/%s", saveDirPath, "mp3", voiceId);

		File wavDir = new File(wavDirPath);
		File mp3Dir = new File(mp3DirPath);

		if (!wavDir.exists()) {
			wavDir.mkdirs();
		}

		if (!mp3Dir.exists()) {
			mp3Dir.mkdirs();
		}

		String hashedFilename = CommonUtil.hashMD5(text);

		File wavFile = new File(wavDir, hashedFilename + ".wav");
		File mp3File = new File(mp3Dir, hashedFilename + ".mp3");

		Result result = agent.getResult(function, input);
		byte[] mp3 = null;
		if (result.getResult() != null && result.getResult() instanceof String) {
			String encodedWav = (String) result.getResult();
			byte[] wavbyte = CommonUtil.base64decoding(encodedWav);

			FileOutputStream fos;
			try {
				fos = new FileOutputStream(wavFile);
				fos.write(wavbyte);
				fos.flush();
				fos.close();

				AudioAttributes audio = new AudioAttributes();
				audio.setCodec("libmp3lame");
				audio.setBitRate(320000);
				audio.setChannels(1);
				audio.setSamplingRate(22050);

				EncodingAttributes attrs = new EncodingAttributes();
				attrs.setFormat("mp3");
				attrs.setAudioAttributes(audio);

				Encoder wavToMp3 = new Encoder();

				wavToMp3.encode(new MultimediaObject(wavFile), mp3File, attrs);

				FileInputStream fis = new FileInputStream(mp3File);
				BufferedInputStream bis = new BufferedInputStream(fis);

				ByteArrayOutputStream buff = new ByteArrayOutputStream();

				byte[] bytebuff = new byte[1024 * 8];
				int readLen = 0;
				while ((readLen = bis.read(bytebuff)) != -1) {
					buff.write(bytebuff, 0, readLen);
				}

				mp3 = buff.toByteArray();

				bis.close();
			} catch (Exception e) {
				logger.error(CommonUtil.getExceptionMessage(e));
			}

		}
		return mp3;
	}

	public byte[] getTtsStreamSelvyMp3(String text) {
		String ttsServer = "192.168.2.154";
		String ttsPort = "6789";

		File tmpFile = null;

		try {
			int language = 0;
			int pitch = 100;
			int speed = 100;
			int volume = 100;
			int sformat = 545;
			int speakerid = 2;

			tmpFile = new File("/tmp", System.currentTimeMillis() + "_" + counter.incrementAndGet());
			String fullName = tmpFile.getCanonicalPath();

			Pttsnet ttsRunner = new Pttsnet();
			logger.info("selvy 음성합성 요청");
			int ret = ttsRunner.PTTSNET_FILE_A_EX(ttsServer, ttsPort, 60, 60, text, fullName, "", "", language, speakerid, sformat, pitch, speed, volume, 0x00, Pttsnet.PTTSNET_CONTENT_PLAIN, Pttsnet.PTTSNET_CHARSET_UTF8, -1, "", 0);
			logger.info("selvy 음성합성 완료");
			if (ret < 0) {
				return null;
			} else {
				File tmpMp3File = new File(fullName + "_mp3");

				AudioAttributes audio = new AudioAttributes();
				audio.setCodec("libmp3lame");
				audio.setBitRate(320000);
				audio.setChannels(1);
				audio.setSamplingRate(22050);

				EncodingAttributes attrs = new EncodingAttributes();
				attrs.setFormat("mp3");
				attrs.setAudioAttributes(audio);

				Encoder wavToMp3 = new Encoder();

				wavToMp3.encode(new MultimediaObject(tmpFile), tmpMp3File, attrs);

				return getdata(tmpMp3File);
			}
		} catch (Exception ce) {
			ce.printStackTrace();
			return null;
		} finally {
			if (tmpFile != null && tmpFile.exists()) {
				tmpFile.delete();
			}
		}
	}

	public byte[] getTtsStreamSelvy(String text) {
		String ttsServer = "192.168.2.154";
		String ttsPort = "6789";

		File tmpFile = null;

		try {
			int language = 0;
			int pitch = 100;
			int speed = 100;
			int volume = 100;
			int sformat = 545;
			int speakerid = 2;

			tmpFile = new File("/tmp", System.currentTimeMillis() + "_" + counter.incrementAndGet());
			String fullName = tmpFile.getCanonicalPath();

			Pttsnet ttsRunner = new Pttsnet();
			int ret = ttsRunner.PTTSNET_FILE_A_EX(ttsServer, ttsPort, 60, 60, text, fullName, "", "", language, speakerid, sformat, pitch, speed, volume, 0x00, Pttsnet.PTTSNET_CONTENT_PLAIN, Pttsnet.PTTSNET_CHARSET_UTF8, -1, "", 0);

			if (ret < 0) {
				return null;
			} else {
				return getdata(tmpFile);
			}
		} catch (Exception ce) {
			ce.printStackTrace();
			return null;
		} finally {
			if (tmpFile != null && tmpFile.exists()) {
				tmpFile.delete();
			}
		}
	}

	private byte[] getdata(File file) throws Exception {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();

		InputStream reader = new FileInputStream(file);
		byte[] buf = new byte[1024];
		int len = 0;
		while ((len = reader.read(buf, 0, 1023)) > -1) {
			bout.write(buf, 0, len);
		}
		reader.close();
		file.delete();
		return bout.toByteArray();
	}
}
