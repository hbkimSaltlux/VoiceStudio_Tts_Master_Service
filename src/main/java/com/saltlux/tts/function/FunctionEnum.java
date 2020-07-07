package com.saltlux.tts.function;

public enum FunctionEnum {

	CONNECT_ENDPOINT(FunctionConst.RESOURCE), FREE_ENDPOINT(FunctionConst.RESOURCE),

	RECOVER_PROCESS(FunctionConst.SERVICE), REQUEST_SERVICE(FunctionConst.SERVICE),
	
	RESET_CACHE(FunctionConst.SERVICE);

	String managetType;

	FunctionEnum(String manageType) {
		this.managetType = manageType;
	}

	public String getManagetType() {
		return managetType;
	}
}
