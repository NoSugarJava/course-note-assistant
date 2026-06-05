package com.noteassistant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class NoteAssistantApplication {

    public static void main(String[] args) {
        SpringApplication.run(NoteAssistantApplication.class, args);
    }
}
