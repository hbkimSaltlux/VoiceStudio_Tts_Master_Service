package com.saltlux.tts;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.apache.http.util.ByteArrayBuffer;

import com.saltlux.tts.agent.output.Result;
import com.saltlux.tts.agent.service.ServiceHelper;
import com.saltlux.tts.util.CommonUtil;

import ws.schild.jave.AudioAttributes;
import ws.schild.jave.Encoder;
import ws.schild.jave.EncodingAttributes;
import ws.schild.jave.MultimediaObject;

public class JaveTest {
	
	public static void main(String[] args) throws Exception {
		Set<String> urls = new LinkedHashSet<>();
		urls.add("http://211.109.9.73:17000/service_broker/ttsstream?voice=pyman_new_gpu&text=");	
		
		File wav = new File("C:\\Users\\hbkim\\Desktop\\TTS\\wavtomp3\\test.wav");
		File mp3 = new File("C:\\Users\\hbkim\\Desktop\\TTS\\wavtomp3\\test.mp3");
		
		ServiceHelper helper = new ServiceHelper("sm2", urls);
		
		Result result = helper.getWavResult("안녕하세요 저는 김한빈입니다 오늘은 날씨가 매우 좋군요 산책하고 싶은데 여섯시에 못마칠 것 같아요", false, false);
		String encodedWav = (String) result.getResult();
		
		long current = System.currentTimeMillis();
		
		byte[] wavbyte = CommonUtil.base64decoding(encodedWav);
		
		FileOutputStream fos = new FileOutputStream(wav);
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
		
		wavToMp3.encode(new MultimediaObject(wav), mp3, attrs);
		
		FileInputStream fis = new FileInputStream(mp3);
		BufferedInputStream bis = new BufferedInputStream(fis);
		
		ByteArrayOutputStream buff = new ByteArrayOutputStream();
		
		System.out.println(mp3.length());
		byte[] bytebuff = new byte[1024];
		int readLen = 0;
		while ((readLen = bis.read(bytebuff)) != -1) {
			System.out.println(readLen);
			buff.write(bytebuff, 0, readLen);
		}
		
		byte[] readMp3 = buff.toByteArray();
		
		bis.close();
		
		File mp3_test = new File("C:\\Users\\hbkim\\Desktop\\TTS\\wavtomp3\\test2.mp3");
		
		fos = new FileOutputStream(mp3_test);
		fos.write(readMp3);
		fos.flush();
		fos.close();
		
	}
	
}
