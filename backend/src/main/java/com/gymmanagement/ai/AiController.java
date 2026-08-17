package com.gymmanagement.ai;

import com.gymmanagement.ai.dto.AskAiRequest;
import com.gymmanagement.ai.dto.AskAiResponse;
import com.gymmanagement.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/ai")
@RequiredArgsConstructor
@Tag(name = "AI", description = "Ask AI assistant for gym management guidance")
public class AiController {

    private final AskAiService askAiService;

    @PostMapping("/ask")
    @Operation(summary = "Ask AI assistant")
    public ApiResponse<AskAiResponse> ask(@Valid @RequestBody AskAiRequest request) {
        return ApiResponse.success(askAiService.ask(request.getQuestion()));
    }
}
