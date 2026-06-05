package com.noteassistant.repository;

import com.noteassistant.model.entity.SlideSet;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SlideSetRepository extends JpaRepository<SlideSet, Long> {
    List<SlideSet> findAllByOrderByCreatedAtDesc();
}
