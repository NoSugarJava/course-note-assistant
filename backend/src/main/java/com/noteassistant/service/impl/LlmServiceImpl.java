package com.noteassistant.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.noteassistant.exception.BusinessException;
import com.noteassistant.service.ConfigService;
import com.noteassistant.service.LlmService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
public class LlmServiceImpl implements LlmService {

    private static final Logger log = LoggerFactory.getLogger(LlmServiceImpl.class);
    private static final String DASHSCOPE_URL =
            "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";

    private final ConfigService configService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public LlmServiceImpl(ConfigService configService, ObjectMapper objectMapper) {
        this.configService = configService;
        this.restTemplate = new RestTemplate();
        this.objectMapper = objectMapper;
    }

    @Override
    public String organizeNotes(String rawText) {
        String apiKey = configService.getRawValue("bailian.apiKey");
        String model = configService.getRawValue("llm.model");

        String promptTemplate = loadPromptTemplate();
        String prompt = promptTemplate.replace("{raw_text}", rawText);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0.3
        );

        try {
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    DASHSCOPE_URL, request, String.class);

            if (response.getStatusCode() != HttpStatus.OK) {
                throw new BusinessException("LLM_API_ERROR",
                        "LLM API returned status: " + response.getStatusCode());
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            String content = root.path("choices").get(0)
                    .path("message").path("content").asText();

            log.info("LLM response received, {} characters", content.length());
            return content;

        } catch (IOException e) {
            throw new BusinessException("LLM_API_ERROR",
                    "Failed to parse LLM response", e);
        } catch (Exception e) {
            throw new BusinessException("LLM_API_ERROR",
                    "LLM API call failed: " + e.getMessage(), e);
        }
    }

    private String loadPromptTemplate() {
        try {
            ClassPathResource resource = new ClassPathResource("prompts/slide-to-note.txt");
            return resource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new BusinessException("INTERNAL_ERROR",
                    "Failed to load prompt template", e);
        }
    }
}
