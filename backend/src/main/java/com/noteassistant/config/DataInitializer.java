package com.noteassistant.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Value("${app.storage.upload-dir:data/uploads}")
    private String uploadDir;

    @Value("${app.storage.note-dir:data/notes}")
    private String noteDir;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Files.createDirectories(Paths.get(uploadDir));
        Files.createDirectories(Paths.get(noteDir));
        log.info("Storage directories initialized: {}, {}", uploadDir, noteDir);
    }
}
