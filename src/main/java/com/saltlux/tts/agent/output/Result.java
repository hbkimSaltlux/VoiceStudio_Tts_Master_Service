package com.saltlux.tts.agent.output;

public class Result {
	private Object result;
	private int errorCode;
	private String errorMessage;

	public Result(Object result) {
		this.result = result;
	}

	public Result(Object result, int errorCode, String errorMessage) {
		this.result = result;
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	public Object getResult() {
		return result;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	@Override
	public String toString() {
		return "Result [result=" + result + ", errorCode=" + errorCode + ", errorMessage=" + errorMessage + "]";
	}
}
