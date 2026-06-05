package com.noteassistant.model.entity;

import com.noteassistant.model.enums.ProcessingStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "slide_image")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlideImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slide_set_id", nullable = false)
    private SlideSet slideSet;

    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;

    @Column(name = "stored_path", nullable = false, length = 500)
    private String storedPath;

    @Column(name = "ocr_text", columnDefinition = "CLOB")
    private String ocrText;

    @Enumerated(EnumType.STRING)
    @Column(name = "ocr_status", nullable = false)
    private ProcessingStatus ocrStatus = ProcessingStatus.PENDING;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
