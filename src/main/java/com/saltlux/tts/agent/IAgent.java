package com.saltlux.tts.agent;

import com.saltlux.tts.agent.output.Result;
import com.saltlux.tts.function.FunctionEnum;

public interface IAgent {
	Result getResult(FunctionEnum functionEnum, String input);
}
