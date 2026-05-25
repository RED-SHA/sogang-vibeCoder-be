# Specification Augmentation — K-의료관광솔루션

## 1. 용어 사전 (Data Dictionary)

### 1.1 User (C01 — abstract)

| 속성명 | 타입 | Not Null | 길이/범위 제한 | 허용값 / 정규식 | 비고 |
|---|---|:---:|---|---|---|
| `userId` | `String` | O | — | UUID v4: `^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$` | 사용자 고유 식별자. 서버 생성(`UUID.randomUUID().toString()`). 외부 입력 수락 금지 |
| `email` | `String` | O | max 320자 | RFC 5321: `^[a-zA-Z0-9._%+\-]+@[a-zA-Z0-9.\-]+\.[a-zA-Z]{2,}$` | OAuth 공급자로부터 수신한 이메일. 로그 출력 시 `***@domain` 형태로 마스킹 필수 |
| `oauthProvider` | `OAuthProviderType` | O | — | `GOOGLE \| KAKAO \| LINE` | 인증 공급자. `OAuthProviderType.valueOf()` 실패 시 400 반환 |
| `oauthSubjectId` | `String` | O | max 255자 | 공백 불가 | OAuth 공급자가 발급한 사용자 고유 ID. **로그에 절대 원문 노출 금지** — 앞 4자리만 표시(`sub_****`) |
| `preferredLanguage` | `Language` | — | — | `EN \| ZH \| JA \| AR \| KO` | null 허용. null 수신 시 `EN`으로 기본 처리 |
| `createdAt` | `LocalDateTime` | O | — | 시스템 생성(`LocalDateTime.now()`) | 최초 생성 시각. 외부 입력값 무시, 서버 시각으로 강제 |
| `lastLoginAt` | `LocalDateTime` | — | — | 시스템 생성 | 최초 생성 시 null. 로그인 성공마다 갱신 |

---

### 1.2 Patient (C03)

| 속성명 | 타입 | Not Null | 길이/범위 제한 | 허용값 / 정규식 | 비고 |
|---|---|:---:|---|---|---|
| `fullNameEn` | `String` | O | 2 ≤ len ≤ 100 | 영문·공백·하이픈·어포스트로피만 허용: `^[A-Za-z\s\-']+$` | 여권과 일치하는 영문 실명. 숫자·특수문자 포함 시 400 |
| `dateOfBirth` | `LocalDate` | O | — | 과거 날짜만 허용(`date < LocalDate.now()`). 최소 `1900-01-01` | 나이 검증. 미래 날짜 또는 1900년 이전 시 400 |
| `nationality` | `String` | O | 정확히 2자 | ISO 3166-1 alpha-2 대문자: `^[A-Z]{2}$` | 국가 코드. `KR`, `US`, `JP` 등. 소문자 입력 시 내부에서 `toUpperCase()` 후 재검증 |
| `onboardingStatus` | `OnboardingStatus` | O | — | `PENDING \| SUBMITTED \| APPROVED`. 초기값 `PENDING` | 신규 환자는 반드시 `PENDING`으로 시작. 외부에서 `APPROVED` 직접 설정 금지 |

---

### 1.3 Staff (C04 — abstract)

| 속성명 | 타입 | Not Null | 길이/범위 제한 | 허용값 / 정규식 | 비고 |
|---|---|:---:|---|---|---|
| `agencyId` | `String` | O | — | UUID v4 형식 | 소속 에이전시. 에이전시 존재 여부 검증 필요 |
| `displayNameEn` | `String` | O | 2 ≤ len ≤ 100 | `^[A-Za-z\s\-']+$` | 환자에게 보이는 영문 표시 이름 |
| `specialization` | `String` | — | max 200자 | — | 전문 분야 (자유 기술). null 허용 |
| `experienceYears` | `Integer` | — | 0 ≤ value ≤ 50 | 음수 또는 50 초과 시 400 | 경력 연수. null 허용(미입력 허용) |
| `availabilityStatus` | `StaffAvailability` | O | — | `AVAILABLE \| ON_DUTY \| OFF_DUTY`. 초기값 `AVAILABLE` | 실시간 배정 가능 여부 |
| `onboardingComplete` | `Boolean` | O | — | `true \| false`. 초기값 `false` | 프로필 설정 완료 여부. `false`인 스태프는 배정 불가 |

---

### 1.4 Agency (C07)

| 속성명 | 타입 | Not Null | 길이/범위 제한 | 허용값 / 정규식 | 비고 |
|---|---|:---:|---|---|---|
| `nameKo` | `String` | O | 1 ≤ len ≤ 100 | — | 한국어 에이전시명 |
| `nameEn` | `String` | O | 2 ≤ len ≤ 100 | 영문·숫자·공백·특수문자 허용 | 영문 에이전시명 |
| `licenseNumber` | `String` | O | 5 ≤ len ≤ 20 | 허가번호 형식: `^[A-Z0-9\-]{5,20}$` | 한국 관광 에이전시 사업 허가번호 |
| `licenseDocumentUrl` | `String` | — | max 1000자 | HTTPS URL: `^https://.*` | 허가증 스캔 파일 URL. HTTP(비암호화) 불가 |
| `contactEmail` | `String` | O | max 320자 | `^[a-zA-Z0-9._%+\-]+@[a-zA-Z0-9.\-]+\.[a-zA-Z]{2,}$` | 에이전시 대표 이메일 |
| `isVerified` | `Boolean` | O | — | `true \| false`. 초기값 `false` | 관리자 검토 완료 여부. API로 직접 `true` 설정 금지 — `verifyAgency()` 메서드만 허용 |
| `portfolioItems` | `List<String>` | — | 항목당 max 500자, 목록 max 20개 | — | 포트폴리오 URL 또는 설명 목록 |

---

### 1.5 PassportInfo (C08)

| 속성명 | 타입 | Not Null | 길이/범위 제한 | 허용값 / 정규식 | 비고 |
|---|---|:---:|---|---|---|
| `patientId` | `String` | O | — | UUID v4 형식 | 소유 환자 ID |
| `imageUrl` | `String` | O | max 1000자 | HTTPS URL: `^https://.*` | 여권 사진 저장 URL. HTTP 불가 (개인정보 보호) |
| `ocrPassportNumber` | `String` | — | 정확히 9자 | ICAO 여권 번호: `^[A-Z][A-Z0-9]{8}$` | OCR 추출 여권 번호. 형식 불일치 시 경고 로그, 오류는 아님 |
| `ocrNationality` | `String` | — | 정확히 3자 | ISO 3166-1 alpha-3: `^[A-Z]{3}$` | OCR 추출 국가 코드 (3자리) |
| `ocrExpiryDate` | `LocalDate` | — | — | 과거 날짜인 경우 WARN 로그 (`[WARN] Passport may be expired`) | OCR 추출 만료일. 만료 여부는 검토자가 판단 |
| `ocrConfidence` | `BigDecimal` | — | 0.00 ≤ value ≤ 1.00, scale(2) | — | OCR 신뢰도. 0.70 미만이면 `WARN` 로그 권장 |
| `confirmedPassportNumber` | `String` | △ | 정확히 9자 | `^[A-Z][A-Z0-9]{8}$` | 검토자 확인 여권 번호. `reviewStatus = APPROVED` 시 필수 |
| `confirmedFullNameEn` | `String` | △ | 2 ≤ len ≤ 100 | `^[A-Za-z\s\-']+$` | 검토자 확인 영문 이름. `reviewStatus = APPROVED` 시 필수 |
| `reviewStatus` | `PassportReviewStatus` | O | — | `PENDING \| APPROVED \| REJECTED`. 초기값 `PENDING` | 검토 상태. 역방향 전이(`APPROVED → PENDING`) 금지 |
| `rejectionReason` | `String` | △ | max 500자 | — | `reviewStatus = REJECTED` 시 필수. 이외에는 null 허용 |
| `reviewedBy` | `String` | △ | — | UUID v4 형식 | `reviewStatus ≠ PENDING` 시 필수 (검토자 관리자 ID) |
| `reviewedAt` | `LocalDateTime` | △ | — | 시스템 생성 | `reviewStatus ≠ PENDING` 시 필수. 외부 입력 금지 |

---

### 1.6 MedicalQuestionnaire (C09)

| 속성명 | 타입 | Not Null | 길이/범위 제한 | 허용값 / 정규식 | 비고 |
|---|---|:---:|---|---|---|
| `patientId` | `String` | O | — | UUID v4 형식 | 소유 환자 ID. 환자당 1건만 허용 |
| `currentMedications` | `List<String>` | — | 항목당 max 200자, 목록 max 50개 | — | 현재 복용 약물 목록. null이면 빈 리스트로 초기화 |
| `allergies` | `List<String>` | — | 항목당 max 200자, 목록 max 30개 | — | 알레르기 목록. null이면 빈 리스트로 초기화 |
| `pastSurgeries` | `List<String>` | — | 항목당 max 300자, 목록 max 20개 | — | 과거 수술 이력 목록. null이면 빈 리스트로 초기화 |
| `medicalNotes` | `String` | — | max 3000자 | — | 기타 의학적 특이사항. 자유 기술 |
| `submittedAt` | `LocalDateTime` | O | — | 시스템 생성 | 제출 시각. 외부 입력 금지 |

---

### 1.7 EmergencyContact (C10)

| 속성명 | 타입 | Not Null | 길이/범위 제한 | 허용값 / 정규식 | 비고 |
|---|---|:---:|---|---|---|
| `patientId` | `String` | O | — | UUID v4 형식 | 소유 환자 ID |
| `fullNameEn` | `String` | O | 2 ≤ len ≤ 100 | `^[A-Za-z\s\-']+$` | 비상 연락처 영문 이름 |
| `relationship` | `String` | O | 1 ≤ len ≤ 50 | — | 관계 (예: `Spouse`, `Parent`, `Sibling`) |
| `phoneE164` | `String` | O | 8 ≤ len ≤ 16 | E.164 형식: `^\+[1-9]\d{6,14}$` | 국제 전화번호. `+` 필수 포함. 위반 시 400 |
| `sortOrder` | `Integer` | O | 1 ≤ value ≤ 2 | `1` 또는 `2` | 환자당 최대 2건 강제. 3 이상 설정 시 400 |

---

### 1.8 QuotationRequest (C11)

| 속성명 | 타입 | Not Null | 길이/범위 제한 | 허용값 / 정규식 | 비고 |
|---|---|:---:|---|---|---|
| `patientId` | `String` | O | — | UUID v4 형식 | 요청 환자 ID |
| `desiredVisitDate` | `LocalDate` | O | — | 오늘 이후: `date.isAfter(LocalDate.now())` | 희망 방문일. 과거 날짜 시 400 |
| `surgeryType` | `String` | O | 1 ≤ len ≤ 200 | — | 희망 수술·시술 종류 (자유 기술) |
| `requiredServices` | `List<RequiredServiceType>` | O | 1개 이상 | `AIRPORT_PICKUP \| ACCOMMODATION \| INTERPRETATION` | 필요 서비스 목록. 빈 목록 시 400 |
| `status` | `QuotationRequestStatus` | O | — | `OPEN \| CLOSED \| EXPIRED`. 초기값 `OPEN` | 요청 상태. 외부에서 `CLOSED`·`EXPIRED` 직접 설정 금지 |
| `expiresAt` | `LocalDateTime` | O | — | 시스템 계산: `createdAt + 7일` | 요청 만료일. 외부 입력 무시, 서버에서 강제 계산 |
| `createdAt` | `LocalDateTime` | O | — | 시스템 생성 | 외부 입력 금지 |

---

### 1.9 ScheduleItem (C15)

| 속성명 | 타입 | Not Null | 길이/범위 제한 | 허용값 / 정규식 | 비고 |
|---|---|:---:|---|---|---|
| `patientJourneyId` | `String` | O | — | UUID v4 형식 | 소속 여정 ID |
| `itemType` | `ScheduleItemType` | O | — | Enum 값 중 하나 | 일정 항목 유형 (HOSPITAL_VISIT, PICKUP 등) |
| `title` | `String` | O | 1 ≤ len ≤ 200 | — | 일정 제목. 공백만으로 구성 불가 |
| `scheduledStartAt` | `LocalDateTime` | O | — | — | 예정 시작 시각 |
| `scheduledEndAt` | `LocalDateTime` | O | — | `scheduledEndAt.isAfter(scheduledStartAt)` 필수 | 예정 종료 시각. 시작 시각 이전 또는 동일 시 400 |
| `locationCoordLat` | `BigDecimal` | — | -90 ≤ value ≤ 90, scale(6) | — | 위도. 범위 위반 시 400 |
| `locationCoordLng` | `BigDecimal` | — | -180 ≤ value ≤ 180, scale(6) | — | 경도. 범위 위반 시 400 |
| `status` | `ScheduleItemStatus` | O | — | `SCHEDULED \| IN_PROGRESS \| COMPLETED`. 초기값 `SCHEDULED` | 진행 상태. **역방향 전이 절대 금지** |
| `isCritical` | `Boolean` | O | — | 초기값 `false` | 핵심 일정(예: 수술) 여부. `true`이면 취소 시 SOS 알림 트리거 |
| `sortOrder` | `Integer` | O | value ≥ 0 | 음수 시 400 | 화면 표시 순서 |
| `memo` | `String` | — | max 1000자 | — | 운영자 메모 |

**상태 전이 검증 규칙**
```
SCHEDULED → IN_PROGRESS 허용
IN_PROGRESS → COMPLETED 허용
그외 모든 전이 → 미허용
```

---

### 1.10 WorkProofPhoto (C18)

| 속성명 | 타입 | Not Null | 길이/범위 제한 | 허용값 / 정규식 | 비고 |
|---|---|:---:|---|---|---|
| `assignmentId` | `String` | O | — | UUID v4 형식 | 소속 스태프 배정 ID |
| `staffId` | `String` | O | — | UUID v4 형식 | 촬영 스태프 ID |
| `fileUrl` | `String` | O | max 1000자 | HTTPS URL: `^https://.*` | 증빙 사진 저장 URL. HTTP 불가 |
| `takenAt` | `LocalDateTime` | O | — | 현재 시각 이전(`takenAt.isBefore(LocalDateTime.now())`) | 촬영 시각. 미래 시각 시 400 |
| `uploadedAt` | `LocalDateTime` | O | — | 시스템 생성 | 업로드 시각. 외부 입력 금지 |
| `retentionExpiresAt` | `LocalDateTime` | O | — | 시스템 계산: `uploadedAt + 1년`. Invoice ISSUED 시 `issuedAt + 1년`으로 갱신 | 보관 만료일. 만료 후 삭제 대상 |

---

### 1.11 ChatMessage (C20)

| 속성명 | 타입 | Not Null | 길이/범위 제한 | 허용값 / 정규식 | 비고 |
|---|---|:---:|---|---|---|
| `conversationId` | `String` | O | — | UUID v4 형식 | 소속 대화방 ID |
| `senderId` | `String` | O | — | UUID v4 형식 | 발송자 사용자 ID |
| `originalText` | `String` | O | 1 ≤ len ≤ 4000 | 공백만으로 구성 불가 | 원문 메시지. 빈 문자열 또는 공백만 시 400 |
| `originalLang` | `Language` | O | — | `EN \| ZH \| JA \| AR \| KO` | 원문 언어. null 시 400 |
| `translatedText` | `String` | — | max 4000자 | — | 번역 결과. null 허용 (번역 전 또는 불필요 시) |
| `translatedLang` | `Language` | — | — | Enum 값 중 하나 | 번역 언어. `translatedText` 있을 시 함께 설정 |
| `sentAt` | `LocalDateTime` | O | — | 시스템 생성 | 발송 시각. 외부 입력 금지 |
| `readAt` | `LocalDateTime` | — | — | 시스템 생성 | 읽음 처리 시각. 최초 null, 읽음 확인 시 갱신 |

---

### 1.12 Invoice (C24)

| 속성명 | 타입 | Not Null | 길이/범위 제한 | 허용값 / 정규식 | 비고 |
|---|---|:---:|---|---|---|
| `patientJourneyId` | `String` | O | — | UUID v4 형식 | 소속 여정 ID |
| `patientId` | `String` | O | — | UUID v4 형식 | 청구 대상 환자 ID |
| `issuedBy` | `String` | O | — | UUID v4 형식 | 발행 관리자 ID |
| `totalAmountUSD` | `BigDecimal` | O | value ≥ 0.00, scale(2) | 음수 금지 | 총액(USD). 개별 항목 합산값과 반드시 일치 |
| `status` | `InvoiceStatus` | O | — | `DRAFT \| ISSUED \| CANCELLED`. 초기값 `DRAFT` | 인보이스 상태 |
| `issuedAt` | `LocalDateTime` | △ | — | 시스템 생성 | `status = ISSUED` 시 필수. `issueInvoice()` 내부에서만 설정 |
| `pdfUrl` | `String` | — | max 1000자 | HTTPS URL | 발행된 PDF 다운로드 URL |

**상태 전이 및 불변 규칙**
```
DRAFT → ISSUED 허용 (issueInvoice)
ISSUED → CANCELLED 허용 (cancelInvoice)
DRAFT → CANCELLED 금지
CANCELLED → * 금지
ISSUED 상태 인보이스의 totalAmountUSD, items 수정 금지
```

---

### 1.13 InvoiceItem (C25)

| 속성명 | 타입 | Not Null | 길이/범위 제한 | 허용값 / 정규식 | 비고 |
|---|---|:---:|---|---|---|
| `invoiceId` | `String` | O | — | UUID v4 형식 | 소속 인보이스 ID |
| `serviceDescription` | `String` | O | 1 ≤ len ≤ 500 | — | 서비스 항목 설명. 공백만 불가 |
| `amountUSD` | `BigDecimal` | O | value ≥ 0.01, scale(2) | 0 이하 금지 | 항목 금액(USD). 0원 항목 불가 |
| `sortOrder` | `Integer` | O | value ≥ 0 | 음수 시 400 | 항목 표시 순서 |

---

### 1.14 AccessLink (C26)

| 속성명 | 타입 | Not Null | 길이/범위 제한 | 허용값 / 정규식 | 비고 |
|---|---|:---:|---|---|---|
| `token` | `String` | O | — | UUID v4 또는 Secure Random hex 64자: `^[0-9a-f]{64}$` | 접근 토큰. 예측 불가능해야 함. `Math.random()` 사용 금지 |
| `linkType` | `AccessLinkType` | O | — | `PATIENT_GUEST_VIEW \| PATIENT_PROPOSAL \| STAFF_INVITATION \| INVOICE_VIEW` | 링크 유형별 만료 시간 결정 |
| `targetId` | `String` | O | — | UUID v4 형식 | 링크가 가리키는 대상 엔티티 ID |
| `recipientUserId` | `String` | — | — | UUID v4 형식 | 수신 대상 사용자 ID. null 허용 (비회원 링크) |
| `expiresAt` | `LocalDateTime` | O | — | 시스템 계산 (유형별 상이, 아래 표 참조) | 만료 시각. 외부 입력 무시 |
| `isInvalidated` | `Boolean` | O | — | 초기값 `false` | 수동 무효화 여부 |
| `failedAttempts` | `Integer` | O | 0 ≤ value | 초기값 `0`. 음수 설정 시 400 | 인증 실패 횟수 |
| `lockedUntil` | `LocalDateTime` | — | — | 시스템 생성 | 5회 초과 실패 시 `LocalDateTime.now() + 15분` 설정 |

**유형별 만료 시간 계산 규칙**

| `linkType` | 만료 = `createdAt +` |
|---|---|
| `PATIENT_GUEST_VIEW` | 72시간 |
| `PATIENT_PROPOSAL` | 72시간 |
| `STAFF_INVITATION` | 24시간 |
| `INVOICE_VIEW` | 30일 |

---

### 1.15 DailyWorkReport (C29)

| 속성명 | 타입 | Not Null | 길이/범위 제한 | 허용값 / 정규식 | 비고 |
|---|---|:---:|---|---|---|
| `staffId` | `String` | O | — | UUID v4 형식 | 제출 스태프 ID |
| `reportDate` | `LocalDate` | O | — | **반드시 오늘 날짜**(`LocalDate.now()`)와 동일해야 함. 과거·미래 날짜 시 400 | 업무 일자 |
| `specialNotes` | `String` | — | max 2000자 | — | 특이사항 기록 |
| `additionalCostUSD` | `BigDecimal` | — | value ≥ 0.00, scale(2) | 음수 시 400 | 추가 비용 발생액(USD). null 허용 (없을 시) |
| `additionalCostNote` | `String` | △ | max 500자 | — | `additionalCostUSD > 0` 시 필수 |
| `submittedAt` | `LocalDateTime` | O | — | 시스템 생성. 반드시 `reportDate`의 자정(`23:59:59`) 이전이어야 함 | 제출 시각. 자정 이후 제출 시 `throw new IllegalStateException("Daily work report must be submitted before midnight of the report date.")` |

---

### 1.16 OAuthLoginRequestDTO

| 속성명 | 타입 | Not Null | 길이/범위 제한 | 허용값 / 정규식 | 비고 |
|---|---|:---:|---|---|---|
| `provider` | `OAuthProviderType` | O | — | `GOOGLE \| KAKAO \| LINE` | 인증 공급자. null 또는 미지원 값 시 400 |
| `authorizationCode` | `String` | O | 1 ≤ len ≤ 2048 | 공백만으로 구성 불가 | OAuth 인가 코드. 로그에 **절대 원문 노출 금지** |
| `redirectUri` | `String` | O | max 2048자 | HTTPS URL: `^https://.*` | 인가 코드 수신 URI. HTTP 불가 |

---

### 1.17 SOSAlertRequestDTO

| 속성명 | 타입 | Not Null | 길이/범위 제한 | 허용값 / 정규식 | 비고 |
|---|---|:---:|---|---|---|
| `staffId` | `String` | O | — | UUID v4 형식 | SOS를 발생시킨 스태프 ID |
| `patientJourneyId` | `String` | O | — | UUID v4 형식 | 관련 여정 ID |
| `locationLat` | `BigDecimal` | O | -90 ≤ value ≤ 90 | 범위 초과 시 400 | 긴급 발생 위도 |
| `locationLng` | `BigDecimal` | O | -180 ≤ value ≤ 180 | 범위 초과 시 400 | 긴급 발생 경도 |
| `notes` | `String` | — | max 1000자 | — | 상황 설명 메모. null 허용 |

---

## 2. 비기능 요구사항 (NFR)

| ID | 분류 | 적용 대상 | 핵심 규칙 요약 | 구현 방법 | 위반 시 결과 |
|---|---|---|---|---|---|
| NFR-SEC-01 | 보안 | `User.email`, `oauthSubjectId`, `AccessLink.token`, `OAuthLoginRequestDTO.authorizationCode`, `PassportInfo.imageUrl` | 5개 속성 로그 원문 출력 금지 — 마스킹 필수 | `MaskingUtil` 정적 메서드 호출 | 개인정보 유출로 간주 |
| NFR-SEC-02 | 보안 | `AccessLink.token` 생성 | `Math.random()` · `new Random()` 사용 금지. `SecureRandom` 또는 `UUID.randomUUID()` 전용 | `AccessLinkController` 내 토큰 생성 로직 교체 | 토큰 예측 가능 → 인증 우회 위험 |
| NFR-SEC-03 | 보안 | `imageUrl`, `fileUrl`, `licenseDocumentUrl`, `pdfUrl`, `redirectUri` (전 URL 필드) | `https://` 시작 강제. `http://` 수신 시 즉시 400 | `ValidationUtil.requireHttpsUrl()` 호출 | `IllegalArgumentException` → HTTP 400 |
| NFR-LOG-01 | 로깅 | 11개 Control 메서드 (로그인·로그아웃·시스템 전환·여권 검토·인보이스 발행·취소·역할 변경·SOS) | `[AUDIT] timestamp \| ACTION= \| ACTOR= \| TARGET= \| RESULT= \| DETAIL=` 포맷으로 콘솔 출력 | `AuditLogger.log()` 정적 메서드 호출 | 감사 추적 불가, 규정 위반 |
| NFR-LOG-02 | 로깅 | 모든 Controller의 `guardNotClosedDown()` | ClosedDown 상태 접근 시 `[WARN] … CLOSED_DOWN_ACCESS` 로그 출력 | `guardNotClosedDown()` 내부에 `AuditLogger.warn()` 추가 | 운영 종료 후 접근 시도 추적 불가 |
| NFR-PERF-01 | 성능 | `SOSController.triggerSOS` | 500ms 이내 완료 목표. 초과 시 `[PERF]` + `[WARN]` 로그 출력. 예외 발생 금지 | 메서드 앞뒤 `System.currentTimeMillis()` 측정 | 경고 로그 (서비스 중단 없음) |
| NFR-PERF-02 | 성능 | `DashboardController.getDashboardData` | 2000ms 초과 시 `[WARN] PERF_DEGRADED` 로그. 부분 데이터라도 반환 | 동일 측정 방식. 빈 DTO 안전 반환 | 경고 로그 (클라이언트에 예외 노출 금지) |
| NFR-PERF-03 | 성능 | `PassportController.uploadPassport` (3000ms), `ChatController.sendMessage` (1000ms) | 처리 시간 항상 `[PERF]` 로그 기록. 임계값 초과 시 `[WARN]` 추가 | `AuditLogger.perf()` 호출 | 경고 로그 |

### 2.1 NFR-SEC-01: 민감 정보 로그 마스킹

다음 속성들은 로그(콘솔 출력 포함)에 원문을 절대 출력하지 않는다. 위반 시 개인정보 유출로 간주한다.

| 대상 클래스 | 속성 | 마스킹 규칙 | 마스킹 예시 |
|---|---|---|---|
| `User` | `email` | `@` 앞부분 3자리 + `***@domain` | `abc***@gmail.com` |
| `User` | `oauthSubjectId` | 앞 4자리만 표시 + `****` | `1234****` |
| `OAuthLoginRequestDTO` | `authorizationCode` | 전체 마스킹 | `[REDACTED]` |
| `AccessLink` | `token` | 앞 8자리만 표시 + `...` | `a1b2c3d4...` |
| `PassportInfo` | `imageUrl` | 도메인 부분만 표시 | `https://storage.ex***.com/***` |

### 2.2 NFR-SEC-02: 접근 링크 토큰 생성 보안

`AccessLink.token` 생성 시 `UUID.randomUUID()` 또는 `SecureRandom` 기반 64자리 hex만 허용한다.

### 2.3 NFR-SEC-03: HTTPS 전용 URL 필드

`imageUrl`, `fileUrl`, `licenseDocumentUrl`, `pdfUrl`, `redirectUri` 등 URL을 저장하는 모든 필드는 `https://`로 시작해야 한다.

### 2.4 NFR-LOG-01: 감사 로그 표준 포맷

다음 Control 메서드 실행 시마다 표준 포맷으로 감사 로그를 콘솔(System.out)에 출력한다. 로그 레벨은 메서드 종류에 따라 구분한다.

**로그 포맷 (ISO 8601 타임스탬프)**
```
[AUDIT] {timestamp} | ACTION={action} | ACTOR={maskedUserId} | TARGET={targetId} | RESULT={SUCCESS|FAIL} | DETAIL={detail}
```

**의무 로깅 대상 메서드**

| Control 클래스 | 메서드 | `action` 값 | `target` |
|---|---|---|---|
| `AuthController` | `loginWithOAuth` | `AUTH_LOGIN` | `userId` |
| `AuthController` | `invalidateSession` | `AUTH_LOGOUT` | `sessionToken` (마스킹) |
| `AuthController` | `startUp` | `SYSTEM_STARTUP` | `"SYSTEM"` |
| `AuthController` | `closeDown` | `SYSTEM_CLOSEDOWN` | `"SYSTEM"` |
| `PassportController` | `reviewPassport` | `PASSPORT_REVIEW` | `passportInfoId` |
| `InvoiceController` | `issueInvoice` | `INVOICE_ISSUED` | `invoiceId` |
| `InvoiceController` | `cancelInvoice` | `INVOICE_CANCELLED` | `invoiceId` |
| `RBACController` | `assignRole` | `ROLE_ASSIGNED` | `userId` |
| `RBACController` | `revokeRole` | `ROLE_REVOKED` | `userId` |
| `SOSController` | `triggerSOS` | `SOS_TRIGGERED` | `patientJourneyId` |
| `SOSController` | `resolveSOS` | `SOS_RESOLVED` | `emergencyAlertId` |

**로깅 구현 규칙**
- 메서드 성공 시: `RESULT=SUCCESS`
- 예외 발생 시: `RESULT=FAIL`, `DETAIL`에 예외 메시지 요약 (원문 그대로 노출 금지 — `sanitize` 처리)
- `ACTOR`(userId)는 마스킹 불필요하되, 길이가 36자(UUID)인 경우 앞 8자리 + `...` 축약 허용

### 2.5 NFR-LOG-02: ClosedDown 상태 접근 시도 로깅

`guardNotClosedDown()`에서 `IllegalStateException`이 발생하는 경우, 다음 포맷으로 경고 로그를 출력한다.
```
[WARN] {timestamp} | CLOSED_DOWN_ACCESS | METHOD={methodName} | ACTOR={userId}
```

### 2.6 NFR-PERF-01: SOS 알림 응답 시간 목표

`SOSController.triggerSOS()` 메서드는 **호출 후 500ms 이내**에 `EmergencyAlert`를 생성하고 알림 발송(PushAdapter 호출)을 시작해야 한다.

500ms 초과 시 다음을 수행한다.
1. `[WARN]` 로그 출력: `[PERF] SOS trigger exceeded 500ms threshold. elapsed={ms}ms`
2. 정상 응답은 유지 (예외 발생 금지 — 지연 감지만)

### 2.7 NFR-PERF-02: 대시보드 조회 타임아웃

`DashboardController.getDashboardData()` 메서드는 내부에서 여러 Controller를 순차 조회하므로, 전체 처리 시간을 측정하여 2000ms 초과 시 경고 로그를 출력하고 부분 데이터를 반환한다.

```
[WARN] {timestamp} | PERF_DEGRADED | ACTION=DASHBOARD_LOAD | elapsed={ms}ms | threshold=2000ms
```

**응답 규격**: 타임아웃이 발생해도 `DashboardDataDTO`를 빈 값으로라도 반환하며 예외를 클라이언트에게 노출하지 않는다.

### 2.8 NFR-PERF-03: 메서드별 처리 시간 기록 대상

다음 메서드는 처리 시간을 콘솔에 항상 기록한다. (`[PERF]` 접두사 사용)

| Control 클래스 | 메서드 | 경고 임계값 |
|---|---|---|
| `SOSController` | `triggerSOS` | 500ms |
| `DashboardController` | `getDashboardData` | 2000ms |
| `PassportController` | `uploadPassport` (OCR 포함) | 3000ms |
| `ChatController` | `sendMessage` (번역 포함) | 1000ms |
