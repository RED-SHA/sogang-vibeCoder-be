package com.k.medtour.domain.journey.repository;

import com.k.medtour.domain.journey.entity.JourneyTemplateItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JourneyTemplateItemRepository extends JpaRepository<JourneyTemplateItem, Long> {

    List<JourneyTemplateItem> findByTemplateIdOrderBySortOrderAsc(Long templateId);

    void deleteByTemplateId(Long templateId);
}
