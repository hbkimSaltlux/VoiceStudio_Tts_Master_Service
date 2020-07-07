package com.saltlux.tts.agent.resource.input;

public class RequestServiceInput {
	private String voiceId;
	private String text;

	@Override
	public String toString() {
		return "RequestServiceInput [voiceId=" + voiceId + ", text=" + text + "]";
	}

	public String getVoiceId() {
		return voiceId;
	}

	public String getText() {
		return text;
	}

}
