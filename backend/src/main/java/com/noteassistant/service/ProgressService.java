package com.noteassistant.service;

import com.noteassistant.model.dto.ProgressEvent;
import com.noteassistant.model.enums.ProcessingStatus;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface ProgressService {
    SseEmitter createEmitter(Long slideSetId);
    void sendProgress(Long slideSetId, int progress, String step, ProcessingStatus status);
    void sendCompleted(Long slideSetId, Long noteId);
    void sendError(Long slideSetId, String errorMessage);
}
