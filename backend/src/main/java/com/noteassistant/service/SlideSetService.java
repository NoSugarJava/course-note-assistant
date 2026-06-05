package com.noteassistant.service;

import com.noteassistant.model.dto.NoteListItem;
import com.noteassistant.model.dto.SlideSetUploadResponse;
import com.noteassistant.model.entity.SlideSet;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface SlideSetService {
    SlideSetUploadResponse upload(MultipartFile[] files, String title);
    SlideSetUploadResponse getStatus(Long slideSetId);
    SlideSet getSlideSet(Long slideSetId);
    void processAsync(Long slideSetId);
    List<NoteListItem> getHistory();
}
