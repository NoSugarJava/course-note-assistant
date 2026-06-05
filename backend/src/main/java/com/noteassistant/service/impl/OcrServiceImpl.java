package com.noteassistant.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.noteassistant.exception.OcrException;
import com.noteassistant.service.ConfigService;
import com.noteassistant.service.OcrService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
public class OcrServiceImpl implements OcrService {

    private static final Logger log = LoggerFactory.getLogger(OcrServiceImpl.class);
    private static final String TOKEN_URL = "https://aip.baidubce.com/oauth/2.0/token";
    private static final String OCR_URL = "https://aip.baidubce.com/rest/2.0/ocr/v1/general_basic";
    private static final long TOKEN_EXPIRY_MS = 29 * 24 * 60 * 60 * 1000L; // 29 days

    private final ConfigService configService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private volatile String cachedToken;
    private volatile long tokenExpiresAt = 0;

    public OcrServiceImpl(ConfigService configService, ObjectMapper objectMapper) {
        this.configService = configService;
        this.restTemplate = new RestTemplate();
        this.objectMapper = objectMapper;
    }

    @Override
    public String recognize(String imagePath) {
        byte[] imageBytes;
        try {
            imageBytes = Files.readAllBytes(Path.of(imagePath));
        } catch (IOException e) {
            throw new OcrException("Failed to read image file: " + imagePath, e);
        }

        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        String accessToken = getAccessToken();

        try {
            String body = "image=" + URLEncoder.encode(base64Image, StandardCharsets.UTF_8);
            String url = OCR_URL + "?access_token=" + accessToken;

            String response = restTemplate.postForObject(url, body, String.class);
            JsonNode root = objectMapper.readTree(response);

            if (root.has("error_code")) {
                String errorMsg = root.path("error_msg").asText("Unknown OCR error");
                throw new OcrException("Baidu OCR error: " + errorMsg);
            }

            StringBuilder text = new StringBuilder();
            JsonNode wordsResult = root.path("words_result");
            if (wordsResult.isArray()) {
                for (JsonNode word : wordsResult) {
                    text.append(word.path("words").asText()).append("\n");
                }
            }
            return text.toString().trim();

        } catch (IOException e) {
            throw new OcrException("Failed to parse OCR response", e);
        }
    }

    @Override
    public String recognizeBatch(List<String> imagePaths) {
        StringBuilder allText = new StringBuilder();
        for (int i = 0; i < imagePaths.size(); i++) {
            log.info("OCR processing image {}/{}", i + 1, imagePaths.size());
            String text = recognize(imagePaths.get(i));
            if (i > 0) {
                allText.append("\n---\n");
            }
            allText.append(text);
        }
        return allText.toString();
    }

    private synchronized String getAccessToken() {
        if (cachedToken != null && System.currentTimeMillis() < tokenExpiresAt) {
            return cachedToken;
        }

        String apiKey = configService.getRawValue("baidu.ocr.apiKey");
        String secretKey = configService.getRawValue("baidu.ocr.secretKey");

        String url = TOKEN_URL + "?grant_type=client_credentials"
                + "&client_id=" + apiKey
                + "&client_secret=" + secretKey;

        try {
            String response = restTemplate.postForObject(url, null, String.class);
            JsonNode root = objectMapper.readTree(response);

            if (root.has("error")) {
                throw new OcrException("Failed to get Baidu access token: "
                        + root.path("error_description").asText("Unknown error"));
            }

            cachedToken = root.path("access_token").asText();
            tokenExpiresAt = System.currentTimeMillis() + TOKEN_EXPIRY_MS;
            log.info("Baidu OCR access token refreshed");
            return cachedToken;

        } catch (IOException e) {
            throw new OcrException("Failed to parse Baidu token response", e);
        }
    }
}
