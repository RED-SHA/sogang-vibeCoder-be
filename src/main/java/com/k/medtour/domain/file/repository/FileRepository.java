package com.k.medtour.domain.file.repository;

import com.k.medtour.domain.file.entity.FileEntity;
import com.k.medtour.domain.file.enums.FileCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FileRepository extends JpaRepository<FileEntity, Long> {

    Optional<FileEntity> findByIdAndDeletedAtIsNull(Long id);

    List<FileEntity> findByUploaderIdAndDeletedAtIsNull(Long uploaderId);

    List<FileEntity> findByUploaderIdAndCategoryAndDeletedAtIsNull(Long uploaderId, FileCategory category);
}
