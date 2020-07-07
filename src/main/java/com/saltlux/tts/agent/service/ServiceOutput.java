package com.saltlux.tts.agent.service;

public class ServiceOutput {
	private int segment;
	private String encodedWav;

	public ServiceOutput(int segment, String encodedWav) {
		this.segment = segment;
		this.encodedWav = encodedWav;
	}

	@Override
	public String toString() {
		return "ServiceResult [segment=" + segment + ", encodedWav=" + encodedWav + "]";
	}

	public int getSegment() {
		return segment;
	}

	public String getEncodedWav() {
		return encodedWav;
	}

}
