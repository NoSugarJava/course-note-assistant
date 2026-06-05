package com.noteassistant.model.entity;

import com.noteassistant.model.enums.ProcessingStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "slide_set")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlideSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProcessingStatus status = ProcessingStatus.PENDING;

    @Column(name = "total_progress")
    private int totalProgress = 0;

    @Column(name = "current_step", length = 100)
    private String currentStep;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "slideSet", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SlideImage> images = new ArrayList<>();

    @OneToOne(mappedBy = "slideSet", cascade = CascadeType.ALL, orphanRemoval = true)
    private Note note;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
