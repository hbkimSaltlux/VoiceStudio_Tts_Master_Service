package com.saltlux.tts;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.saltlux.tts.agent.ResourceAgent;
import com.saltlux.tts.agent.output.Result;
import com.saltlux.tts.agent.service.input.StreamInput;
import com.saltlux.tts.function.FunctionEnum;
import com.saltlux.tts.manager.BrokerManager;
import com.saltlux.tts.util.CommonUtil;

@Controller
public class ApiController {

	@RequestMapping(value = "/test", method = RequestMethod.GET)
	@ResponseBody
	public String test() throws Exception {
		return new Gson().toJson(ResourceAgent.getCopyEndpointMap());
	}

	@RequestMapping(value = "/tts", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
	@ResponseBody
	public String ttsstream(@RequestParam(value = "text", required = true) String message, @RequestParam(value = "voice", required = true) String voice, @RequestParam(value = "cache", required = false, defaultValue = "false") String useCache, @RequestParam(value = "replace", required = false, defaultValue = "false") String replace, @RequestParam(value = "type", required = false, defaultValue = "wav") String type) throws Exception {
		// check input text length, status code == 100, max text length == 50
		StreamInput input = new StreamInput(voice, message, useCache, replace);
		BrokerManager manager = BrokerManager.getInstance();
		String result = null;
		if (type.equals("wav")) {
			result = manager.getTtsJson(FunctionEnum.REQUEST_SERVICE, input.toJson());
		} else {
			result = manager.getTtsJsonMp3(FunctionEnum.REQUEST_SERVICE, input.toJson());
		}
		return result;
	}

	@RequestMapping(value = "/ttsstream", method = RequestMethod.GET, produces = { MediaType.APPLICATION_OCTET_STREAM_VALUE })
	@ResponseBody
	public HttpEntity<byte[]> ttsstreambyte(@RequestParam(value = "text", required = true) String message, @RequestParam(value = "voice", required = true) String voice, @RequestParam(value = "cache", required = false, defaultValue = "false") String useCache, @RequestParam(value = "replace", required = false, defaultValue = "false") String replace, @RequestParam(value = "type", required = false, defaultValue = "wav") String type) throws Exception {
		StreamInput input = new StreamInput(voice, message, useCache, replace);
		BrokerManager manager = BrokerManager.getInstance();
		byte[] result = null;
		HttpHeaders header = new HttpHeaders();

		if (voice.equals("0")) {
			if (type.equals("mp3")) {
				result = manager.getTtsStreamSelvyMp3(message);
				header.setContentType(new MediaType("audio", "mpeg"));
				header.setContentLength(result.length);
				return new HttpEntity<byte[]>(result, header);
			} else if (type.equals("wav")) {
				result = manager.getTtsStreamSelvy(message);
				header.setContentType(new MediaType("audio", "wav"));
				header.setContentLength(result.length);
				return new HttpEntity<byte[]>(result, header);
			} else {
				result = manager.getTtsStreamSelvy(message);
				header.setContentType(new MediaType("audio", "wav"));
				header.setContentLength(result.length);
				return new HttpEntity<byte[]>(result, header);
			}
		}

		if (type.equals("wav")) {
			result = manager.getTtsStream(FunctionEnum.REQUEST_SERVICE, input.toJson());
			header.setContentType(new MediaType("audio", "wav"));
			header.setContentLength(result.length);
			return new HttpEntity<byte[]>(result, header);
		} else if (type.equals("mp3")) {
			result = manager.getTtsStreamMp3(FunctionEnum.REQUEST_SERVICE, input.toJson());
			header.setContentType(new MediaType("audio", "mpeg"));
			header.setContentLength(result.length);
			return new HttpEntity<byte[]>(result, header);
		} else {
			result = manager.getTtsStream(FunctionEnum.REQUEST_SERVICE, input.toJson());
			header.setContentType(new MediaType("audio", "wav"));
			header.setContentLength(result.length);
			return new HttpEntity<byte[]>(result, header);
		}
	}
	
//	@RequestMapping(value = "/ttsstream/test", method = RequestMethod.GET, produces = { "application/json;charset=UTF-8" })
//	@ResponseBody
//	public ResponseEntity<String> ttsstreambyteTest(@RequestParam(value = "text", required = true) String message, @RequestParam(value = "voice", required = true) String voice, @RequestParam(value = "cache", required = false, defaultValue = "true") String useCache, @RequestParam(value = "replace", required = false, defaultValue = "false") String replace, @RequestParam(value = "type", required = false, defaultValue = "wav") String type) throws Exception {
//		StreamInput input = new StreamInput(voice, message, useCache, replace);
//		BrokerManager manager = BrokerManager.getInstance();
//		byte[] result = null;
//		HttpHeaders header = new HttpHeaders();
//		
//		Map<String, String> testMap = new HashMap<String, String>();
//		testMap.put("json", "[{\"extensionPoint\":\"WeatherInfo\",\"parameters\":[{\"name\":\"location\",\"type\":\"java.lang.String\"},{\"name\":\"time\",\"type\":\"java.lang.String\"}]}]");
//		
//		
////		MultiValueMap<String, byte[]> mvm = new LinkedMultiValueMap<>();
////		mvm.add("json", "[{\"extensionPoint\":\"WeatherInfo\",\"parameters\":[{\"name\":\"location\",\"type\":\"java.lang.String\"},{\"name\":\"time\",\"type\":\"java.lang.String\"}]}]".getBytes());
////		mvm.add("wave", result);
//		
//
//		if (type.equals("wav")) {
//			result = manager.getTtsStream(FunctionEnum.REQUEST_SERVICE, input.toJson());
////			header.setContentType(new MediaType("multipart", "form-data"));
//			header.setContentLength(result.length);
//
//			testMap.put("wave", CommonUtil.base64encoding(result));
//			return new ResponseEntity<String>(new Gson().toJson(testMap), header, HttpStatus.OK);
//		} 
//		
//		return null;
//	}

	@RequestMapping(value = "/connect", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
	@ResponseBody
	public String connectEndpoint(@RequestBody String input) throws Exception {
		BrokerManager manager = BrokerManager.getInstance();
		String result = manager.getResult(FunctionEnum.CONNECT_ENDPOINT, input);

		return result;
	}

	@RequestMapping(value = "/free", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
	@ResponseBody
	public String freeEndpoint(@RequestBody String input) throws Exception {
		BrokerManager manager = BrokerManager.getInstance();
		String result = manager.getResult(FunctionEnum.FREE_ENDPOINT, input);

		return result;
	}

	@RequestMapping(value = "/recover", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
	@ResponseBody
	public String activateUrl(@RequestBody String input) throws Exception {
		BrokerManager manager = BrokerManager.getInstance();
		String result = manager.getResult(FunctionEnum.RECOVER_PROCESS, input);

		return result;
	}

	@RequestMapping(value = "/reset/cache", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
	@ResponseBody
	public String resetCache(@RequestParam(value = "voice", required = true) String voiceId, @RequestParam(value = "id", required = true) String id, @RequestParam(value = "passwd", required = true) String password) throws Exception {
		if (id.equals("admin") && password.equals("reset!cache")) {
			BrokerManager manager = BrokerManager.getInstance();
			String result = manager.getResult(FunctionEnum.RESET_CACHE, voiceId);
			return result;
		} else {
			Result result = new Result(null, -1, "Invalid ID or Password");
			Gson gson = new GsonBuilder().serializeNulls().create();
			return gson.toJson(result);
		}
	}
}
