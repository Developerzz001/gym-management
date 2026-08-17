package com.gymmanagement.ai;

import com.gymmanagement.ai.dto.AskAiResponse;

public interface AskAiService {

    AskAiResponse ask(String question);
}
