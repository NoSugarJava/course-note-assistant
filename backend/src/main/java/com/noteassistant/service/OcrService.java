package com.noteassistant.service;

public interface OcrService {
    /**
     * Recognize text from a single image file.
     * @return recognized text (empty string if nothing recognized)
     */
    String recognize(String imagePath);

    /**
     * Batch recognize: calls recognize() for each path sequentially.
     * @return concatenated text with separators
     */
    String recognizeBatch(java.util.List<String> imagePaths);
}
