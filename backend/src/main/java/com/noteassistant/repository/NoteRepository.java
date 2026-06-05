package com.noteassistant.repository;

import com.noteassistant.model.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findAllByOrderByCreatedAtDesc();
    Optional<Note> findBySlideSetId(Long slideSetId);
}
