package com.noteassistant.service;

import com.noteassistant.model.dto.NoteListItem;
import com.noteassistant.model.dto.NoteResponse;
import com.noteassistant.model.entity.Note;
import com.noteassistant.model.entity.SlideSet;

import java.util.List;

public interface NoteService {
    Note createNote(SlideSet slideSet, String markdownContent);
    NoteResponse getNote(Long noteId);
    List<NoteListItem> getAllNotes();
    void deleteNote(Long noteId);
    byte[] getNoteAsMarkdownBytes(Long noteId);
}
