# Sprint 3 Contract: File Upload

## 범위

### 요구사항
- PAT-201 (여권 OCR 이미지), PAT-502 (의료 사진/문서 보안 전송), STA-402 (현장 사진 업로드)
- Cross-cutting: 다른 도메인에서 파일 참조 시 사용

### 구현할 엔드포인트 (3개)
- `POST /api/v1/files/upload` — 범용 파일 업로드 (multipart/form-data, 최대 20MB)
- `GET /api/v1/files/{fileId}/download` — Presigned URL 생성
- `DELETE /api/v1/files/{fileId}` — 파일 삭제 (소프트 삭제)

### 생성할 Entity
- FileEntity — id, uploader_id (FK member), original_name, stored_name, mime_type, file_size, category (enum), s3_key, url, created_at, deleted_at

### Enum
- FileCategory (PASSPORT, MEDICAL_PHOTO, CHAT_FILE, PROOF_PHOTO, DOCUMENT)

## 수락 기준

### 빌드
- [ ] `./gradlew compileJava` 성공
- [ ] `./gradlew compileTestJava` 성공

### 테스트
- [ ] FileService 단위 테스트 (업로드/다운로드URL/삭제)
- [ ] FileController @WebMvcTest 슬라이스 테스트
- [ ] @DisplayName 한국어

### 아키텍처
- [ ] ApiResponse<T> 래핑
- [ ] 파일 타입 검증 (image/jpeg, image/png, application/pdf 등)
- [ ] 파일 크기 검증 (20MB 제한)
- [ ] 본인 파일만 삭제 가능 (ADMIN은 전체)
- [ ] Flyway V4 마이그레이션
- [ ] S3 연동은 인터페이스 기반 (MVP: 로컬 스토리지 구현)

### 보안
- [ ] Presigned URL 유효시간 1시간
- [ ] 파일 접근 권한 검증

## 예상 파일 수: ~15개
## 의존성: Sprint 1 완료
