package com.k.medtour.domain.journey.repository;

import com.k.medtour.domain.journey.entity.JourneyTemplate;
import com.k.medtour.domain.journey.enums.TemplateCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface JourneyTemplateRepository extends JpaRepository<JourneyTemplate, Long> {

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    Optional<JourneyTemplate> findByIdAndDeletedAtIsNull(Long id);

    @Query("SELECT t FROM JourneyTemplate t WHERE t.deletedAt IS NULL " +
            "AND (:keyword IS NULL OR LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:category IS NULL OR t.category = :category)")
    Page<JourneyTemplate> findAllByFilters(
            @Param("keyword") String keyword,
            @Param("category") TemplateCategory category,
            Pageable pageable
    );
}
