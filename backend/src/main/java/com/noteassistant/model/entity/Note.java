package com.noteassistant.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "note")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slide_set_id", nullable = false, unique = true)
    private SlideSet slideSet;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "markdown_content", columnDefinition = "CLOB", nullable = false)
    private String markdownContent;

    @Column(name = "stored_path", nullable = false, length = 500)
    private String storedPath;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
