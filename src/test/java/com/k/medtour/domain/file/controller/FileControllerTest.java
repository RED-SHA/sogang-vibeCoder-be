package com.k.medtour.domain.file.controller;

import com.k.medtour.domain.file.dto.FileDownloadResponse;
import com.k.medtour.domain.file.dto.FileUploadResponse;
import com.k.medtour.domain.file.enums.FileCategory;
import com.k.medtour.domain.file.service.FileService;
import com.k.medtour.global.auth.jwt.JwtTokenProvider;
import com.k.medtour.global.exception.BusinessException;
import com.k.medtour.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FileController.class)
@AutoConfigureMockMvc(addFilters = false)
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FileService fileService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Nested
    @DisplayName("POST /api/v1/files/upload")
    class UploadApiTest {

        @Test
        @DisplayName("성공 - 파일을 업로드하면 201을 반환한다")
        @WithMockUser(roles = "PATIENT")
        void upload_success() throws Exception {
            // Given
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.jpg", "image/jpeg", new byte[1024]);

            FileUploadResponse response = new FileUploadResponse(
                    1L, "test.jpg", 1024L, "image/jpeg",
                    FileCategory.PASSPORT,
                    "http://localhost:8080/uploads/passport/uuid.jpg",
                    LocalDateTime.now());

            given(fileService.upload(any(), anyString(), any()))
                    .willReturn(response);

            // When & Then
            mockMvc.perform(multipart("/api/v1/files/upload")
                            .file(file)
                            .param("category", "PASSPORT"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.fileName").value("test.jpg"))
                    .andExpect(jsonPath("$.data.mimeType").value("image/jpeg"))
                    .andExpect(jsonPath("$.data.category").value("PASSPORT"));
        }

        @Test
        @DisplayName("실패 - 파일 크기 초과 시 400을 반환한다")
        @WithMockUser(roles = "PATIENT")
        void upload_fail_sizeExceeded() throws Exception {
            // Given
            MockMultipartFile file = new MockMultipartFile(
                    "file", "large.jpg", "image/jpeg", new byte[100]);

            given(fileService.upload(any(), anyString(), any()))
                    .willThrow(new BusinessException(ErrorCode.FILE_SIZE_EXCEEDED));

            // When & Then
            mockMvc.perform(multipart("/api/v1/files/upload")
                            .file(file)
                            .param("category", "PASSPORT"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 허용되지 않은 파일 형식이면 400을 반환한다")
        @WithMockUser(roles = "PATIENT")
        void upload_fail_invalidType() throws Exception {
            // Given
            MockMultipartFile file = new MockMultipartFile(
                    "file", "script.exe", "application/x-msdownload", new byte[100]);

            given(fileService.upload(any(), anyString(), any()))
                    .willThrow(new BusinessException(ErrorCode.INVALID_FILE_TYPE));

            // When & Then
            mockMvc.perform(multipart("/api/v1/files/upload")
                            .file(file)
                            .param("category", "DOCUMENT"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 유효하지 않은 카테고리면 400을 반환한다")
        @WithMockUser(roles = "PATIENT")
        void upload_fail_invalidCategory() throws Exception {
            // Given
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.jpg", "image/jpeg", new byte[100]);

            given(fileService.upload(any(), anyString(), any()))
                    .willThrow(new BusinessException(ErrorCode.INVALID_FILE_CATEGORY));

            // When & Then
            mockMvc.perform(multipart("/api/v1/files/upload")
                            .file(file)
                            .param("category", "INVALID"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/files/{fileId}/download")
    class DownloadApiTest {

        @Test
        @DisplayName("성공 - 다운로드 URL을 생성하여 반환한다")
        @WithMockUser(roles = "PATIENT")
        void getDownloadUrl_success() throws Exception {
            // Given
            FileDownloadResponse response = new FileDownloadResponse(
                    1L, "test.jpg",
                    "http://localhost:8080/uploads/passport/uuid.jpg",
                    LocalDateTime.now().plusHours(1));

            given(fileService.getDownloadUrl(1L)).willReturn(response);

            // When & Then
            mockMvc.perform(get("/api/v1/files/1/download"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.fileId").value(1))
                    .andExpect(jsonPath("$.data.fileName").value("test.jpg"))
                    .andExpect(jsonPath("$.data.downloadUrl").exists());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 파일이면 404를 반환한다")
        @WithMockUser(roles = "PATIENT")
        void getDownloadUrl_fail_notFound() throws Exception {
            // Given
            given(fileService.getDownloadUrl(999L))
                    .willThrow(new BusinessException(ErrorCode.FILE_NOT_FOUND));

            // When & Then
            mockMvc.perform(get("/api/v1/files/999/download"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/files/{fileId}")
    class DeleteApiTest {

        @Test
        @DisplayName("성공 - 파일을 삭제하면 200을 반환한다")
        @WithMockUser(roles = "PATIENT")
        void delete_success() throws Exception {
            // Given
            doNothing().when(fileService).delete(anyLong(), any(), anyString());

            // When & Then
            mockMvc.perform(delete("/api/v1/files/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("실패 - 타인의 파일 삭제 시 403을 반환한다")
        @WithMockUser(roles = "PATIENT")
        void delete_fail_accessDenied() throws Exception {
            // Given
            doThrow(new BusinessException(ErrorCode.FILE_ACCESS_DENIED))
                    .when(fileService).delete(anyLong(), any(), anyString());

            // When & Then
            mockMvc.perform(delete("/api/v1/files/1"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 파일 삭제 시 404를 반환한다")
        @WithMockUser(roles = "PATIENT")
        void delete_fail_notFound() throws Exception {
            // Given
            doThrow(new BusinessException(ErrorCode.FILE_NOT_FOUND))
                    .when(fileService).delete(anyLong(), any(), anyString());

            // When & Then
            mockMvc.perform(delete("/api/v1/files/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }
}
