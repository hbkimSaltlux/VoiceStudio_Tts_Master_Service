package com.saltlux.tts.agent;

import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.saltlux.tts.agent.output.Result;
import com.saltlux.tts.agent.resource.input.ConnectEndpointInput;
import com.saltlux.tts.agent.resource.input.VoiceIdInput;
import com.saltlux.tts.agent.service.input.ProcessRecoveryInput;
import com.saltlux.tts.agent.service.input.StreamInput;
import com.saltlux.tts.function.FunctionEnum;

public class Agent implements IAgent {
	private static final Logger logger = LoggerFactory.getLogger(Agent.class);
	
	private ResourceAgent resourceAgent;
	private ServiceAgent serviceAgent;

	public Agent(ResourceAgent resourceAgent, ServiceAgent serviceAgent) {
		this.resourceAgent = resourceAgent;
		this.serviceAgent = serviceAgent;
		initAgent();
	}
	
	private void initAgent() {
		Map<String, Set<String>> copyMap = ResourceAgent.getCopyEndpointMap();
		serviceAgent.serviceRecovery(copyMap);
	}
	
	public ServiceAgent getServiceAgent() {
		return serviceAgent;
	}

	@Override
	public Result getResult(FunctionEnum functionEnum, String input) {
		Result result = null;

		Gson gson = new Gson();
		switch (functionEnum) {
		case CONNECT_ENDPOINT:
			logger.info("connect input : {}", input);
			ConnectEndpointInput connectEndpointInput = gson.fromJson(input, ConnectEndpointInput.class);
			result = resourceAgent.connectEndpoint(connectEndpointInput);
			logger.info("connect result : {}", result);
			if ((boolean) result.getResult()) {
				serviceAgent.serviceConnect(connectEndpointInput);
			}
			break;
		case FREE_ENDPOINT:
			logger.info("free input : {}", input);
			VoiceIdInput voiceIdInput = gson.fromJson(input, VoiceIdInput.class);
			result = resourceAgent.freeEndpoint(voiceIdInput);
			logger.info("free result : {}", result);
			if ((boolean) result.getResult()) {
				serviceAgent.serviceFree(voiceIdInput);
			}
			break;
		case REQUEST_SERVICE:
			logger.info("request service : {}", input);
			StreamInput streamInput = gson.fromJson(input, StreamInput.class);
			resourceAgent.saveRequestText(streamInput.getVoiceId(), streamInput.getText());
			String replaceText = resourceAgent.replaceText(streamInput.getText());
			if (!replaceText.equals(streamInput.getText())) {
				streamInput.setText(replaceText);
				logger.info("replaced input text : {}", streamInput);
			}
			result = serviceAgent.getRequestService(streamInput);	
			if (result.getResult() != null) {
				resourceAgent.saveCacheWave(result, streamInput);
			}
			break;
		case RECOVER_PROCESS:
			logger.info("process recovery : {}", input);
			ProcessRecoveryInput recoveryInput = gson.fromJson(input, ProcessRecoveryInput.class);
			result = serviceAgent.processRecovery(recoveryInput);
			break;
		case RESET_CACHE:
			logger.info("reset cache voiceId : {}", input);
			if (serviceAgent.hasService(input)) {
				serviceAgent.resetCache(input);
				resourceAgent.removeCache(input);
				result = new Result(input + " reset cache");
			} else {
				result = new Result(null, -1, "invalid voice id");
			}
		default:
			break;
		}
		return result;
	}

}
