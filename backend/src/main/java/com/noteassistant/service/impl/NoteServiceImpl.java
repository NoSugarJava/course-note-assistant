package com.noteassistant.service.impl;

import com.noteassistant.exception.BusinessException;
import com.noteassistant.model.dto.NoteListItem;
import com.noteassistant.model.dto.NoteResponse;
import com.noteassistant.model.entity.Note;
import com.noteassistant.model.entity.SlideSet;
import com.noteassistant.repository.NoteRepository;
import com.noteassistant.service.NoteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class NoteServiceImpl implements NoteService {

    private static final Logger log = LoggerFactory.getLogger(NoteServiceImpl.class);

    @Value("${app.storage.note-dir:data/notes}")
    private String noteDir;

    private final NoteRepository noteRepository;

    public NoteServiceImpl(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    @Override
    @Transactional
    public Note createNote(SlideSet slideSet, String markdownContent) {
        String safeTitle = slideSet.getTitle().replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fff_-]", "_");
        String filename = slideSet.getId() + "-" + safeTitle + ".md";
        Path filePath = Paths.get(noteDir, filename);

        try {
            Files.createDirectories(filePath.getParent());
            Files.writeString(filePath, markdownContent, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new BusinessException("INTERNAL_ERROR",
                    "Failed to write note file: " + e.getMessage(), e);
        }

        Note note = Note.builder()
                .slideSet(slideSet)
                .title(slideSet.getTitle())
                .markdownContent(markdownContent)
                .storedPath(filePath.toString())
                .build();

        Note saved = noteRepository.save(note);
        log.info("Note created: id={}, path={}", saved.getId(), saved.getStoredPath());
        return saved;
    }

    @Override
    public NoteResponse getNote(Long noteId) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new BusinessException("NOT_FOUND",
                        "Note not found: " + noteId));
        return toResponse(note);
    }

    @Override
    public List<NoteListItem> getAllNotes() {
        return noteRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toListItem)
                .toList();
    }

    @Override
    @Transactional
    public void deleteNote(Long noteId) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new BusinessException("NOT_FOUND",
                        "Note not found: " + noteId));

        // Delete file from disk
        try {
            Files.deleteIfExists(Path.of(note.getStoredPath()));
        } catch (IOException e) {
            log.warn("Failed to delete note file: {}", note.getStoredPath());
        }

        // Delete associated upload images
        SlideSet slideSet = note.getSlideSet();
        if (slideSet != null && slideSet.getImages() != null) {
            for (var img : slideSet.getImages()) {
                try {
                    Files.deleteIfExists(Path.of(img.getStoredPath()));
                } catch (IOException e) {
                    log.warn("Failed to delete image file: {}", img.getStoredPath());
                }
            }
        }

        noteRepository.delete(note);
        log.info("Note deleted: id={}", noteId);
    }

    @Override
    public byte[] getNoteAsMarkdownBytes(Long noteId) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new BusinessException("NOT_FOUND",
                        "Note not found: " + noteId));
        return note.getMarkdownContent().getBytes(StandardCharsets.UTF_8);
    }

    private NoteResponse toResponse(Note note) {
        return NoteResponse.builder()
                .id(note.getId())
                .title(note.getTitle())
                .markdownContent(note.getMarkdownContent())
                .createdAt(note.getCreatedAt())
                .build();
    }

    private NoteListItem toListItem(Note note) {
        int imageCount = note.getSlideSet() != null && note.getSlideSet().getImages() != null
                ? note.getSlideSet().getImages().size() : 0;
        return NoteListItem.builder()
                .id(note.getId())
                .title(note.getTitle())
                .imageCount(imageCount)
                .createdAt(note.getCreatedAt())
                .build();
    }
}
