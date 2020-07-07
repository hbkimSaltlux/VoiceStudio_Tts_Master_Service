package com.saltlux.tts.agent.service.input;

import com.google.gson.Gson;

public class StreamInput {
	private String voiceId;
	private String text;
	private boolean useCache;
	private boolean replace;

	public StreamInput(String voiceId, String text, String useCache, String replace) {
		this.voiceId = voiceId;
		this.text = text;
		if ("true".equals(useCache.toLowerCase())) {
			this.useCache = true;
		} else {
			this.useCache = false;
		}
		if ("true".equals(replace.toLowerCase())) {
			this.replace = true;
		} else {
			this.replace = false;
		}
	}

	@Override
	public String toString() {
		return "StreamInput [voiceId=" + voiceId + ", text=" + text + ", useCache=" + useCache + ", replace=" + replace + "]";
	}

	public String getVoiceId() {
		return voiceId;
	}

	public String getText() {
		return text;
	}

	public boolean isUseCache() {
		return useCache;
	}

	public boolean isReplace() {
		return replace;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String toJson() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}

}
