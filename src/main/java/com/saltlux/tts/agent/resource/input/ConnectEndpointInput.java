package com.saltlux.tts.agent.resource.input;

import java.util.Set;

public class ConnectEndpointInput {
	private String voiceId;
	private Set<String> urlSet;

	public String getVoiceId() {
		return voiceId;
	}

	public Set<String> getUrlSet() {
		return urlSet;
	}

	@Override
	public String toString() {
		return "ConnectEndpointInput [voiceId=" + voiceId + ", urlSet=" + urlSet + "]";
	}
}
