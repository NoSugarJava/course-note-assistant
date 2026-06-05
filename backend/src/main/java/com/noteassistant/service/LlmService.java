package com.noteassistant.service;

public interface LlmService {
    /**
     * Send concatenated OCR text to the LLM to organize into structured Markdown notes.
     * @param rawText the OCR-extracted text
     * @return organized Markdown notes
     */
    String organizeNotes(String rawText);
}
