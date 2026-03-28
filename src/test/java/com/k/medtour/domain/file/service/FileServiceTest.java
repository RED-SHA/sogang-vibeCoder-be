package com.k.medtour.domain.file.service;

import com.k.medtour.domain.file.dto.FileDownloadResponse;
import com.k.medtour.domain.file.dto.FileUploadResponse;
import com.k.medtour.domain.file.entity.FileEntity;
import com.k.medtour.domain.file.enums.FileCategory;
import com.k.medtour.domain.file.repository.FileRepository;
import com.k.medtour.global.exception.BusinessException;
import com.k.medtour.global.exception.ErrorCode;
import com.k.medtour.infra.s3.StorageService;
import com.k.medtour.infra.s3.StorageService.StorageUploadResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @InjectMocks
    private FileService fileService;

    @Mock
    private FileRepository fileRepository;

    @Mock
    private StorageService storageService;

    @Nested
    @DisplayName("파일 업로드")
    class UploadTest {

        @Test
        @DisplayName("성공 - JPEG 이미지 파일을 업로드한다")
        void upload_success() {
            // Given
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.jpg", "image/jpeg", new byte[1024]);
            StorageUploadResult uploadResult = new StorageUploadResult(
                    "passport/uuid.jpg", "http://localhost:8080/uploads/passport/uuid.jpg");

            given(storageService.upload(any(), anyString(), eq("PASSPORT")))
                    .willReturn(uploadResult);
            given(fileRepository.save(any(FileEntity.class)))
                    .willAnswer(invocation -> {
                        FileEntity entity = invocation.getArgument(0);
                        return FileEntity.builder()
                                .uploaderId(entity.getUploaderId())
                                .originalName(entity.getOriginalName())
                                .storedName(entity.getStoredName())
                                .mimeType(entity.getMimeType())
                                .fileSize(entity.getFileSize())
                                .category(entity.getCategory())
                                .s3Key(entity.getS3Key())
                                .url(entity.getUrl())
                                .build();
                    });

            // When
            FileUploadResponse response = fileService.upload(file, "PASSPORT", 1L);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.fileName()).isEqualTo("test.jpg");
            assertThat(response.mimeType()).isEqualTo("image/jpeg");
            assertThat(response.category()).isEqualTo(FileCategory.PASSPORT);
            verify(storageService).upload(any(), anyString(), eq("PASSPORT"));
            verify(fileRepository).save(any(FileEntity.class));
        }

        @Test
        @DisplayName("성공 - PDF 문서를 업로드한다")
        void upload_success_pdf() {
            // Given
            MockMultipartFile file = new MockMultipartFile(
                    "file", "report.pdf", "application/pdf", new byte[2048]);
            StorageUploadResult uploadResult = new StorageUploadResult(
                    "document/uuid.pdf", "http://localhost:8080/uploads/document/uuid.pdf");

            given(storageService.upload(any(), anyString(), eq("DOCUMENT")))
                    .willReturn(uploadResult);
            given(fileRepository.save(any(FileEntity.class)))
                    .willAnswer(invocation -> {
                        FileEntity entity = invocation.getArgument(0);
                        return FileEntity.builder()
                                .uploaderId(entity.getUploaderId())
                                .originalName(entity.getOriginalName())
                                .storedName(entity.getStoredName())
                                .mimeType(entity.getMimeType())
                                .fileSize(entity.getFileSize())
                                .category(entity.getCategory())
                                .s3Key(entity.getS3Key())
                                .url(entity.getUrl())
                                .build();
                    });

            // When
            FileUploadResponse response = fileService.upload(file, "DOCUMENT", 1L);

            // Then
            assertThat(response.fileName()).isEqualTo("report.pdf");
            assertThat(response.category()).isEqualTo(FileCategory.DOCUMENT);
        }

        @Test
        @DisplayName("실패 - 파일 크기가 20MB를 초과하면 예외가 발생한다")
        void upload_fail_fileSizeExceeded() {
            // Given
            byte[] largeContent = new byte[21 * 1024 * 1024]; // 21MB
            MockMultipartFile file = new MockMultipartFile(
                    "file", "large.jpg", "image/jpeg", largeContent);

            // When & Then
            assertThatThrownBy(() -> fileService.upload(file, "PASSPORT", 1L))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.FILE_SIZE_EXCEEDED);

            verify(storageService, never()).upload(any(), anyString(), anyString());
        }

        @Test
        @DisplayName("실패 - 허용되지 않은 파일 형식이면 예외가 발생한다")
        void upload_fail_invalidMimeType() {
            // Given
            MockMultipartFile file = new MockMultipartFile(
                    "file", "script.exe", "application/x-msdownload", new byte[100]);

            // When & Then
            assertThatThrownBy(() -> fileService.upload(file, "DOCUMENT", 1L))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.INVALID_FILE_TYPE);

            verify(storageService, never()).upload(any(), anyString(), anyString());
        }

        @Test
        @DisplayName("실패 - 유효하지 않은 카테고리면 예외가 발생한다")
        void upload_fail_invalidCategory() {
            // Given
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.jpg", "image/jpeg", new byte[100]);

            // When & Then
            assertThatThrownBy(() -> fileService.upload(file, "INVALID_CATEGORY", 1L))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.INVALID_FILE_CATEGORY);
        }
    }

    @Nested
    @DisplayName("다운로드 URL 생성")
    class GetDownloadUrlTest {

        @Test
        @DisplayName("성공 - Presigned URL을 생성하여 반환한다")
        void getDownloadUrl_success() {
            // Given
            FileEntity fileEntity = FileEntity.builder()
                    .uploaderId(1L)
                    .originalName("test.jpg")
                    .storedName("uuid.jpg")
                    .mimeType("image/jpeg")
                    .fileSize(1024L)
                    .category(FileCategory.PASSPORT)
                    .s3Key("passport/uuid.jpg")
                    .url("http://localhost:8080/uploads/passport/uuid.jpg")
                    .build();

            given(fileRepository.findByIdAndDeletedAtIsNull(1L))
                    .willReturn(Optional.of(fileEntity));
            given(storageService.generatePresignedUrl("passport/uuid.jpg"))
                    .willReturn("http://localhost:8080/uploads/passport/uuid.jpg");

            // When
            FileDownloadResponse response = fileService.getDownloadUrl(1L);

            // Then
            assertThat(response.fileName()).isEqualTo("test.jpg");
            assertThat(response.downloadUrl()).contains("passport/uuid.jpg");
            assertThat(response.expiresAt()).isNotNull();
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 파일이면 예외가 발생한다")
        void getDownloadUrl_fail_notFound() {
            // Given
            given(fileRepository.findByIdAndDeletedAtIsNull(999L))
                    .willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> fileService.getDownloadUrl(999L))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.FILE_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("파일 삭제")
    class DeleteTest {

        @Test
        @DisplayName("성공 - 본인의 파일을 삭제한다")
        void delete_success_owner() {
            // Given
            FileEntity fileEntity = FileEntity.builder()
                    .uploaderId(1L)
                    .originalName("test.jpg")
                    .storedName("uuid.jpg")
                    .mimeType("image/jpeg")
                    .fileSize(1024L)
                    .category(FileCategory.PASSPORT)
                    .s3Key("passport/uuid.jpg")
                    .url("http://localhost:8080/uploads/passport/uuid.jpg")
                    .build();

            given(fileRepository.findByIdAndDeletedAtIsNull(1L))
                    .willReturn(Optional.of(fileEntity));

            // When
            fileService.delete(1L, 1L, "PATIENT");

            // Then
            assertThat(fileEntity.isDeleted()).isTrue();
            verify(storageService).delete("passport/uuid.jpg");
        }

        @Test
        @DisplayName("성공 - ADMIN은 타인의 파일도 삭제할 수 있다")
        void delete_success_admin() {
            // Given
            FileEntity fileEntity = FileEntity.builder()
                    .uploaderId(1L)
                    .originalName("test.jpg")
                    .storedName("uuid.jpg")
                    .mimeType("image/jpeg")
                    .fileSize(1024L)
                    .category(FileCategory.PASSPORT)
                    .s3Key("passport/uuid.jpg")
                    .url("http://localhost:8080/uploads/passport/uuid.jpg")
                    .build();

            given(fileRepository.findByIdAndDeletedAtIsNull(1L))
                    .willReturn(Optional.of(fileEntity));

            // When
            fileService.delete(1L, 99L, "ADMIN");

            // Then
            assertThat(fileEntity.isDeleted()).isTrue();
            verify(storageService).delete("passport/uuid.jpg");
        }

        @Test
        @DisplayName("실패 - 타인의 파일 삭제 시 권한 예외가 발생한다")
        void delete_fail_accessDenied() {
            // Given
            FileEntity fileEntity = FileEntity.builder()
                    .uploaderId(1L)
                    .originalName("test.jpg")
                    .storedName("uuid.jpg")
                    .mimeType("image/jpeg")
                    .fileSize(1024L)
                    .category(FileCategory.PASSPORT)
                    .s3Key("passport/uuid.jpg")
                    .url("http://localhost:8080/uploads/passport/uuid.jpg")
                    .build();

            given(fileRepository.findByIdAndDeletedAtIsNull(1L))
                    .willReturn(Optional.of(fileEntity));

            // When & Then
            assertThatThrownBy(() -> fileService.delete(1L, 99L, "PATIENT"))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.FILE_ACCESS_DENIED);

            verify(storageService, never()).delete(anyString());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 파일 삭제 시 예외가 발생한다")
        void delete_fail_notFound() {
            // Given
            given(fileRepository.findByIdAndDeletedAtIsNull(999L))
                    .willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> fileService.delete(999L, 1L, "PATIENT"))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.FILE_NOT_FOUND);
        }
    }
}
