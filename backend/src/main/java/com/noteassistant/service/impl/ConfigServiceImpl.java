package com.noteassistant.service.impl;

import com.noteassistant.exception.BusinessException;
import com.noteassistant.model.entity.AppConfig;
import com.noteassistant.repository.AppConfigRepository;
import com.noteassistant.service.ConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ConfigServiceImpl implements ConfigService {

    private static final Logger log = LoggerFactory.getLogger(ConfigServiceImpl.class);
    private static final String[] PREDEFINED_KEYS = {
            "baidu.ocr.apiKey",
            "baidu.ocr.secretKey",
            "bailian.apiKey",
            "llm.model"
    };
    private static final String DEFAULT_LLM_MODEL = "qwen-plus";

    private final AppConfigRepository repository;
    private final ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<>();

    public ConfigServiceImpl(AppConfigRepository repository) {
        this.repository = repository;
    }

    @jakarta.annotation.PostConstruct
    public void init() {
        seedDefaultKeys();
        refreshCache();
    }

    private void seedDefaultKeys() {
        for (String key : PREDEFINED_KEYS) {
            if (repository.findByConfigKey(key).isEmpty()) {
                String defaultValue = key.equals("llm.model") ? DEFAULT_LLM_MODEL : "";
                repository.save(AppConfig.builder()
                        .configKey(key)
                        .configValue(defaultValue)
                        .updatedAt(LocalDateTime.now())
                        .build());
                log.info("Seeded config key: {}", key);
            }
        }
    }

    private void refreshCache() {
        List<AppConfig> all = repository.findAll();
        for (AppConfig c : all) {
            cache.put(c.getConfigKey(), c.getConfigValue());
        }
    }

    @Override
    public Map<String, String> getAllMasked() {
        Map<String, String> result = new HashMap<>();
        cache.forEach((key, value) -> {
            result.put(key, maskValue(value));
        });
        return result;
    }

    @Override
    @Transactional
    public void update(Map<String, String> updates) {
        for (var entry : updates.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (value == null || value.isBlank()) {
                throw new BusinessException("VALIDATION_ERROR",
                        "Config value for '" + key + "' cannot be empty");
            }
            AppConfig config = repository.findByConfigKey(key)
                    .orElseGet(() -> AppConfig.builder()
                            .configKey(key)
                            .configValue("")
                            .build());
            config.setConfigValue(value);
            repository.save(config);
            cache.put(key, value);
        }
        log.info("Updated {} config entries", updates.size());
    }

    @Override
    public String getRawValue(String key) {
        String value = cache.get(key);
        if (value == null || value.isEmpty()) {
            throw new BusinessException("CONFIG_MISSING",
                    "Configuration '" + key + "' is not set. Please configure it in Settings.");
        }
        return value;
    }

    private String maskValue(String value) {
        if (value == null || value.length() <= 6) {
            return "***";
        }
        return value.substring(0, 3) + "***" + value.substring(value.length() - 3);
    }
}
