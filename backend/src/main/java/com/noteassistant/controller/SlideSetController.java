package com.noteassistant.controller;

import com.noteassistant.model.dto.SlideSetUploadResponse;
import com.noteassistant.service.ProgressService;
import com.noteassistant.service.SlideSetService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/slidesets")
public class SlideSetController {

    private final SlideSetService slideSetService;
    private final ProgressService progressService;

    public SlideSetController(SlideSetService slideSetService, ProgressService progressService) {
        this.slideSetService = slideSetService;
        this.progressService = progressService;
    }

    @PostMapping
    public ResponseEntity<SlideSetUploadResponse> upload(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "title", required = false) String title) {
        SlideSetUploadResponse response = slideSetService.upload(files, title);
        slideSetService.processAsync(response.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SlideSetUploadResponse> getStatus(@PathVariable Long id) {
        return ResponseEntity.ok(slideSetService.getStatus(id));
    }

    @GetMapping(value = "/{id}/progress", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamProgress(@PathVariable Long id) {
        return progressService.createEmitter(id);
    }
}
