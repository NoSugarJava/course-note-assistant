package com.noteassistant.service;

import java.util.Map;

public interface ConfigService {
    Map<String, String> getAllMasked();
    void update(Map<String, String> updates);
    String getRawValue(String key);
}
