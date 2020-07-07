package com.saltlux.tts.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class WavMerger {

	// header size 58 용
//	private static final int HEADER_SIZE = 58;
//	private static final int HEADER_OFFSET = 36;
//	private static final int LEAD_BYTE= 4;
//	private static final int AFTER_DATA_OFFSET = 54;

//	header size 44용
	private static final int HEADER_SIZE = 44;
	private static final int HEADER_OFFSET = 36;
	private static final int LEAD_BYTE = 4;
	private static final int AFTER_DATA_OFFSET = 40;

	public static void encodeInt(int in, byte[] out, int offset) {
		out[offset + 0] = (byte) (in & 0xFF);
		out[offset + 1] = (byte) ((in >> 8) & 0xFF);
		out[offset + 2] = (byte) ((in >> 16) & 0xFF);
		out[offset + 3] = (byte) ((in >> 24) & 0xFF);
	}

	public static byte[] readWaveFile(String fileName) throws Exception {
		File file = new File(fileName);
		FileInputStream fis = new FileInputStream(file);
		byte bytes[] = new byte[(int) file.length()];
		fis.read(bytes);
		fis.close();
		return bytes;
	}

	public static void writeWaveFile(byte[] wave, String fileName) throws Exception {
		FileOutputStream fos = new FileOutputStream(fileName);
		fos.write(wave);
		fos.close();
	}

	public static byte[] merge(List<SimpleEntry<byte[], Integer>> waves) {

		if (waves == null || waves.isEmpty()) {
			return null;
		}

//		if (waves.size() == 1) {
//			return waves.get(0).getKey();
//		}

		ByteArrayOutputStream bout = null;
		try {
			bout = new ByteArrayOutputStream();
			int TotalDataSize = 0;
			for (SimpleEntry<byte[], Integer> wavinfo : waves) {
				byte[] wav = wavinfo.getKey();
				
				if (TotalDataSize == 0) {
					bout.write(wav, 0, wav.length);
				} else {
					bout.write(wav, HEADER_SIZE, (wav.length - HEADER_SIZE));
				}
				TotalDataSize += wav.length - HEADER_SIZE;
			}
			byte[] mergeData = bout.toByteArray();
			encodeInt(TotalDataSize + HEADER_OFFSET, mergeData, LEAD_BYTE);
			encodeInt(TotalDataSize, mergeData, AFTER_DATA_OFFSET);
			return mergeData;

		} finally {
			try {
				if (bout != null) {
					bout.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) throws Exception {
		List<SimpleEntry<byte[], Integer>> waves = new ArrayList<>();
		waves.add(new SimpleEntry<>(readWaveFile("c:\\temp\\1.wav"), 0));
		waves.add(new SimpleEntry<>(readWaveFile("c:\\temp\\2.wav"), 1));
		waves.add(new SimpleEntry<>(readWaveFile("c:\\temp\\3.wav"), 2));

		byte[] mergeWav = merge(waves);
		writeWaveFile(mergeWav, "c:\\temp\\merge.wav");

	}
}
