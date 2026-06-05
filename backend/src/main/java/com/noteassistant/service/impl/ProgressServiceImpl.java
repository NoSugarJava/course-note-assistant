package com.noteassistant.service.impl;

import com.noteassistant.model.dto.ProgressEvent;
import com.noteassistant.model.enums.ProcessingStatus;
import com.noteassistant.service.ProgressService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ProgressServiceImpl implements ProgressService {

    private static final Logger log = LoggerFactory.getLogger(ProgressServiceImpl.class);
    private static final long SSE_TIMEOUT = 10 * 60 * 1000L; // 10 minutes

    private final ConcurrentHashMap<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    @Override
    public SseEmitter createEmitter(Long slideSetId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        emitters.put(slideSetId, emitter);

        emitter.onCompletion(() -> {
            log.debug("SSE completed for slideSet {}", slideSetId);
            emitters.remove(slideSetId);
        });
        emitter.onTimeout(() -> {
            log.debug("SSE timeout for slideSet {}", slideSetId);
            emitters.remove(slideSetId);
        });
        emitter.onError(e -> {
            log.debug("SSE error for slideSet {}: {}", slideSetId, e.getMessage());
            emitters.remove(slideSetId);
        });

        return emitter;
    }

    @Override
    public void sendProgress(Long slideSetId, int progress, String step, ProcessingStatus status) {
        ProgressEvent event = ProgressEvent.builder()
                .slideSetId(slideSetId)
                .progress(progress)
                .step(step)
                .status(status)
                .build();
        send(slideSetId, event);
    }

    @Override
    public void sendCompleted(Long slideSetId, Long noteId) {
        ProgressEvent event = ProgressEvent.builder()
                .slideSetId(slideSetId)
                .progress(100)
                .step("Completed")
                .status(ProcessingStatus.COMPLETED)
                .noteId(noteId)
                .build();
        send(slideSetId, event);
        close(slideSetId);
    }

    @Override
    public void sendError(Long slideSetId, String errorMessage) {
        ProgressEvent event = ProgressEvent.builder()
                .slideSetId(slideSetId)
                .step("Error")
                .status(ProcessingStatus.FAILED)
                .errorMessage(errorMessage)
                .build();
        send(slideSetId, event);
        close(slideSetId);
    }

    private void send(Long slideSetId, ProgressEvent event) {
        SseEmitter emitter = emitters.get(slideSetId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("progress")
                        .data(event));
            } catch (IOException e) {
                log.warn("Failed to send SSE for slideSet {}: {}", slideSetId, e.getMessage());
                emitters.remove(slideSetId);
            }
        }
    }

    private void close(Long slideSetId) {
        SseEmitter emitter = emitters.remove(slideSetId);
        if (emitter != null) {
            try {
                emitter.complete();
            } catch (Exception e) {
                // ignore
            }
        }
    }
}
