package com.saltlux.tts.agent.service.input;

public class ProcessRecoveryInput {
	private String voiceId;
	private String url;
	private boolean activate;
	
	@Override
	public String toString() {
		return "ProcessRecoveryInput [voiceId=" + voiceId + ", url=" + url + ", activate=" + activate + "]";
	}

	public String getVoiceId() {
		return voiceId;
	}

	public String getUrl() {
		return url;
	}

	public boolean isActivate() {
		return activate;
	}
	
}
