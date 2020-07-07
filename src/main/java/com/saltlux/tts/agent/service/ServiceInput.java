package com.saltlux.tts.agent.service;

public class ServiceInput {
	private String voiceId;
	private String url;
	private String key;
	private int segment;
	private String text;

	@Override
	public String toString() {
		return "ServiceInput [voiceId=" + voiceId + ", url=" + url + ", key=" + key + ", segment=" + segment + ", text="
				+ text + "]";
	}


	public ServiceInput(String voiceId, String url, String key, int segment, String text) {
		this.voiceId = voiceId;
		this.url = url;
		this.key = key;
		this.segment = segment;
		this.text = text;
	}


	public String getKey() {
		return key;
	}

	public int getSegment() {
		return segment;
	}

	public String getText() {
		return text;
	}

	public String getVoiceId() {
		return voiceId;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + segment;
		result = prime * result + ((text == null) ? 0 : text.hashCode());
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		result = prime * result + ((voiceId == null) ? 0 : voiceId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ServiceInput other = (ServiceInput) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		if (segment != other.segment)
			return false;
		if (text == null) {
			if (other.text != null)
				return false;
		} else if (!text.equals(other.text))
			return false;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		if (voiceId == null) {
			if (other.voiceId != null)
				return false;
		} else if (!voiceId.equals(other.voiceId))
			return false;
		return true;
	}

}
