package com.noteassistant.service.impl;

import com.noteassistant.exception.BusinessException;
import com.noteassistant.exception.OcrException;
import com.noteassistant.model.dto.NoteListItem;
import com.noteassistant.model.dto.SlideSetUploadResponse;
import com.noteassistant.model.entity.Note;
import com.noteassistant.model.entity.SlideImage;
import com.noteassistant.model.entity.SlideSet;
import com.noteassistant.model.enums.ProcessingStatus;
import com.noteassistant.repository.SlideSetRepository;
import com.noteassistant.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class SlideSetServiceImpl implements SlideSetService {

    private static final Logger log = LoggerFactory.getLogger(SlideSetServiceImpl.class);

    @Value("${app.storage.upload-dir:data/uploads}")
    private String uploadDir;

    @Value("${app.ocr.max-image-count:20}")
    private int maxImageCount;

    @Value("${app.ocr.max-image-size:20971520}")
    private long maxImageSize;

    private final SlideSetRepository slideSetRepository;
    private final OcrService ocrService;
    private final LlmService llmService;
    private final NoteService noteService;
    private final ProgressService progressService;

    public SlideSetServiceImpl(SlideSetRepository slideSetRepository,
                               OcrService ocrService,
                               LlmService llmService,
                               NoteService noteService,
                               ProgressService progressService) {
        this.slideSetRepository = slideSetRepository;
        this.ocrService = ocrService;
        this.llmService = llmService;
        this.noteService = noteService;
        this.progressService = progressService;
    }

    @Override
    @Transactional
    public SlideSetUploadResponse upload(MultipartFile[] files, String title) {
        if (files == null || files.length == 0) {
            throw new BusinessException("VALIDATION_ERROR", "At least one image is required");
        }
        if (files.length > maxImageCount) {
            throw new BusinessException("UPLOAD_TOO_MANY",
                    "Maximum " + maxImageCount + " images allowed");
        }

        if (title == null || title.isBlank()) {
            title = "Untitled-" + LocalDateTime.now().toString().replace(":", "-").substring(0, 19);
        }

        SlideSet slideSet = SlideSet.builder()
                .title(title)
                .status(ProcessingStatus.PENDING)
                .build();
        slideSet = slideSetRepository.save(slideSet);

        List<SlideImage> images = new ArrayList<>();
        try {
            Path uploadPath = Paths.get(uploadDir, slideSet.getId().toString());
            Files.createDirectories(uploadPath);

            for (int i = 0; i < files.length; i++) {
                MultipartFile file = files[i];
                if (file.getSize() > maxImageSize) {
                    throw new BusinessException("UPLOAD_TOO_LARGE",
                            "File '" + file.getOriginalFilename() + "' exceeds 20MB limit");
                }

                String ext = getExtension(file.getOriginalFilename());
                String storedName = UUID.randomUUID().toString() + ext;
                Path destPath = uploadPath.resolve(storedName);
                file.transferTo(destPath.toFile());

                SlideImage image = SlideImage.builder()
                        .slideSet(slideSet)
                        .originalFilename(file.getOriginalFilename())
                        .storedPath(destPath.toString())
                        .ocrStatus(ProcessingStatus.PENDING)
                        .sortOrder(i)
                        .build();
                images.add(image);
            }
        } catch (IOException e) {
            throw new BusinessException("INTERNAL_ERROR",
                    "Failed to save uploaded files: " + e.getMessage(), e);
        }

        slideSet.setImages(images);
        slideSet = slideSetRepository.save(slideSet);

        log.info("SlideSet created: id={}, images={}", slideSet.getId(), images.size());

        return SlideSetUploadResponse.builder()
                .id(slideSet.getId())
                .title(slideSet.getTitle())
                .status(slideSet.getStatus())
                .imageCount(images.size())
                .createdAt(slideSet.getCreatedAt())
                .build();
    }

    @Override
    public SlideSetUploadResponse getStatus(Long slideSetId) {
        SlideSet slideSet = getSlideSet(slideSetId);
        return SlideSetUploadResponse.builder()
                .id(slideSet.getId())
                .title(slideSet.getTitle())
                .status(slideSet.getStatus())
                .totalProgress(slideSet.getTotalProgress())
                .currentStep(slideSet.getCurrentStep())
                .imageCount(slideSet.getImages().size())
                .createdAt(slideSet.getCreatedAt())
                .build();
    }

    @Override
    public SlideSet getSlideSet(Long slideSetId) {
        return slideSetRepository.findById(slideSetId)
                .orElseThrow(() -> new BusinessException("NOT_FOUND",
                        "SlideSet not found: " + slideSetId));
    }

    @Override
    @Async("taskExecutor")
    public void processAsync(Long slideSetId) {
        SlideSet slideSet = getSlideSet(slideSetId);
        log.info("Starting async processing for slideSet {}", slideSetId);

        try {
            // Step 1: Progress - starting
            updateProgress(slideSet, 10, "Preparing images...", ProcessingStatus.OCR_PROCESSING);

            // Step 2: Collect image paths
            List<SlideImage> images = slideSet.getImages();
            List<String> imagePaths = images.stream()
                    .map(SlideImage::getStoredPath)
                    .toList();

            // Step 3: OCR each image
            StringBuilder allText = new StringBuilder();
            for (int i = 0; i < imagePaths.size(); i++) {
                int progress = 15 + (int) (40.0 * (i + 1) / imagePaths.size());
                updateProgress(slideSet, progress,
                        "OCR image " + (i + 1) + " of " + imagePaths.size() + "...",
                        ProcessingStatus.OCR_PROCESSING);

                try {
                    String text = ocrService.recognize(imagePaths.get(i));
                    images.get(i).setOcrText(text);
                    images.get(i).setOcrStatus(ProcessingStatus.COMPLETED);
                    if (i > 0) allText.append("\n---\n");
                    allText.append(text);
                } catch (OcrException e) {
                    images.get(i).setOcrStatus(ProcessingStatus.FAILED);
                    throw e;
                }
            }
            slideSetRepository.save(slideSet);

            // Step 4: Concatenation complete
            updateProgress(slideSet, 55, "OCR complete, preparing for AI organization...",
                    ProcessingStatus.LLM_PROCESSING);

            // Step 5: LLM organization
            updateProgress(slideSet, 60, "AI is organizing notes...",
                    ProcessingStatus.LLM_PROCESSING);

            String markdown = llmService.organizeNotes(allText.toString());

            updateProgress(slideSet, 90, "AI organization complete, saving...",
                    ProcessingStatus.LLM_PROCESSING);

            // Step 6: Save note
            Note note = noteService.createNote(slideSet, markdown);

            // Step 7: Complete
            slideSet.setStatus(ProcessingStatus.COMPLETED);
            slideSet.setTotalProgress(100);
            slideSet.setCurrentStep("Completed");
            slideSetRepository.save(slideSet);

            log.info("SlideSet processing completed: slideSetId={}, noteId={}", slideSetId, note.getId());
            progressService.sendCompleted(slideSetId, note.getId());

        } catch (Exception e) {
            log.error("SlideSet processing failed: slideSetId={}", slideSetId, e);
            slideSet.setStatus(ProcessingStatus.FAILED);
            slideSet.setErrorMessage(e.getMessage());
            slideSetRepository.save(slideSet);
            progressService.sendError(slideSetId, e.getMessage());
        }
    }

    @Override
    public List<NoteListItem> getHistory() {
        return noteService.getAllNotes();
    }

    private void updateProgress(SlideSet slideSet, int progress, String step, ProcessingStatus status) {
        slideSet.setTotalProgress(progress);
        slideSet.setCurrentStep(step);
        slideSet.setStatus(status);
        slideSetRepository.save(slideSet);
        progressService.sendProgress(slideSet.getId(), progress, step, status);
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf("."));
    }
}
