package com.noteassistant.controller;

import com.noteassistant.model.dto.ConfigUpdateRequest;
import com.noteassistant.service.ConfigService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/config")
public class ConfigController {

    private final ConfigService configService;

    public ConfigController(ConfigService configService) {
        this.configService = configService;
    }

    @GetMapping
    public ResponseEntity<Map<String, String>> getAll() {
        return ResponseEntity.ok(configService.getAllMasked());
    }

    @PutMapping
    public ResponseEntity<Map<String, String>> update(@Valid @RequestBody ConfigUpdateRequest request) {
        configService.update(request.getUpdates());
        return ResponseEntity.ok(configService.getAllMasked());
    }
}
