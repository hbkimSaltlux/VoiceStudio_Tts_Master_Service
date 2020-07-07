package com.saltlux.tts.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.springframework.core.io.ClassPathResource;

public class VoiceMapper {

	private static VoiceMapper instance;
	private final String voiceIdConf = "voice.ini";

	public static synchronized VoiceMapper getinstance() {
		if (instance == null) {
			instance = new VoiceMapper();
		}
		return instance;
	}

	private VoiceMapper() {
		init();
	}

	private String defaultVoice = null;
	private Map<String, Vector<String>> voiceMap = new HashMap<>();

	public Map<String, Vector<String>> getVoiceMap() {
		return voiceMap;
	}

	private void init() {
		try {
			ClassPathResource classPathResouce = new ClassPathResource(voiceIdConf);

			System.out.println(this.getClass().getResource(voiceIdConf));
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(classPathResouce.getInputStream(), "UTF-8"));
			String line = null;
			while ((line = reader.readLine()) != null) {
				if (line.length() == 0 || line.charAt(0) == '#') {
					continue;
				}
				String[] sp = line.split("\t");
				if (sp.length != 2) {
					System.out.println("Error in " + line);
					continue;
				}
				String key = sp[0];
				String value = sp[1];

				if (defaultVoice == null) {
					defaultVoice = value;
				}
				if (!voiceMap.containsKey(key)) {
					Vector<String> vector = new Vector<>();
					vector.add(value);
					voiceMap.put(key, vector);
				}
			}

			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*public void updateEndpoint() {

		try {
			ClassPathResource classPathResouce = new ClassPathResource(voiceIdConf);
			File file = classPathResouce.getFile();
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));

			if (file.isFile() && file.canWrite()) {
				// 쓰기
				voiceMap.forEach((k, v) -> writer.write(k + "\t" + v + "\n"));

				writer.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/

}
