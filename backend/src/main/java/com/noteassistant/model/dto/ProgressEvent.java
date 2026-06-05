package com.noteassistant.model.dto;

import com.noteassistant.model.enums.ProcessingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgressEvent {
    private Long slideSetId;
    private int progress;
    private String step;
    private ProcessingStatus status;
    private Long noteId;
    private String errorMessage;
}
