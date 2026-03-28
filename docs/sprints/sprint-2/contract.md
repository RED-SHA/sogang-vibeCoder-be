# Sprint 2 Contract: Patient Onboarding

## 범위

### 요구사항
- PAT-201: 여권 정보 업로드 (Manual/OCR)
- PAT-202: 영문 의료/알레르기 문진표
- PAT-203: 긴급 연락처 등록
- ADM-201: 환자 DB 및 서류 검토

### 구현할 엔드포인트 (11개)

#### Patient API
- `POST /api/v1/patients/me/passport` — 여권 정보 업로드
- `GET /api/v1/patients/{patientId}/passport` — 여권 정보 조회
- `PUT /api/v1/patients/me/passport` — 여권 정보 수정
- `POST /api/v1/patients/me/medical-questionnaire` — 문진표 제출
- `GET /api/v1/patients/{patientId}/medical-questionnaire` — 문진표 조회
- `PUT /api/v1/patients/me/medical-questionnaire` — 문진표 수정
- `POST /api/v1/patients/me/emergency-contacts` — 긴급 연락처 등록
- `GET /api/v1/patients/{patientId}/emergency-contacts` — 긴급 연락처 조회
- `PUT /api/v1/patients/me/emergency-contacts/{contactId}` — 긴급 연락처 수정
- `DELETE /api/v1/patients/me/emergency-contacts/{contactId}` — 긴급 연락처 삭제
- `GET /api/v1/admin/patients` — 관리자 환자 목록 조회 (ADM-201)

### 생성할 Entity
- PatientPassport (여권 정보)
- MedicalQuestionnaire (의료 문진표 - JSONB)
- EmergencyContact (긴급 연락처)

## 수락 기준

### 빌드
- [ ] `./gradlew compileJava` 성공
- [ ] `./gradlew compileTestJava` 성공

### 테스트
- [ ] PatientService 단위 테스트 (여권/문진표/긴급연락처 CRUD)
- [ ] PatientController @WebMvcTest 슬라이스 테스트
- [ ] @DisplayName 한국어, public 메서드당 성공+실패

### 아키텍처
- [ ] ApiResponse<T> 래핑
- [ ] @PreAuthorize (PATIENT/ADMIN)
- [ ] 본인 데이터 접근 검증 (Service에서 memberId 체크)
- [ ] 여권번호 마스킹 (응답 시 뒷자리 4자 마스킹)
- [ ] Flyway V3 마이그레이션

### 보안
- [ ] 환자 본인 + ADMIN만 여권/문진표 접근 가능
- [ ] 여권번호 로그 출력 금지

### DB
- [ ] BaseEntity 상속
- [ ] FetchType.LAZY
- [ ] 문진표 상세(알레르기, 약물 등)는 JSONB

## 예상 파일 수: ~25개
## 의존성: Sprint 1 완료
