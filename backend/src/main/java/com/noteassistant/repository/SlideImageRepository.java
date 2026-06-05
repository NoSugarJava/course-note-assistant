package com.noteassistant.repository;

import com.noteassistant.model.entity.SlideImage;
import com.noteassistant.model.entity.SlideSet;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SlideImageRepository extends JpaRepository<SlideImage, Long> {
    List<SlideImage> findBySlideSetOrderBySortOrderAsc(SlideSet slideSet);
}
