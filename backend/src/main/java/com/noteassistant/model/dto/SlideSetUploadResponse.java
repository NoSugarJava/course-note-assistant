package com.noteassistant.model.dto;

import com.noteassistant.model.enums.ProcessingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlideSetUploadResponse {
    private Long id;
    private String title;
    private ProcessingStatus status;
    private int totalProgress;
    private String currentStep;
    private int imageCount;
    private LocalDateTime createdAt;
}
