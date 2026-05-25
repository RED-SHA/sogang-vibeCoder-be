# NEXT_AGENT_PROMPT — K-의료관광솔루션

## 상태: 완료

K-의료관광솔루션의 전체 소스 파일 생성이 완료되었습니다.
추가 요구사항 반영이나 컴파일 검증 등으로 세션을 이어가야 할 경우 아래 내용을 참고하세요.

---

## 프로젝트 개요

**출력 경로:** `/Users/mac/sogang/sogang-vibeCoder-be/Output_L3+L4`

**아키텍처:** BCE (Boundary-Control-Entity) / Robustness 패턴 — Pure Java SE (Spring, Lombok 사용 안 함)

**설계 문서:**
- L3: `/Users/mac/sogang/K의료관광솔루션/docs/L3_Static_Modeling_K의료관광솔루션.md`
- L4: `/Users/mac/sogang/K의료관광솔루션/docs/L4_Object_Structuring_K의료관광솔루션.md`

---

## 패키지 구조

```
com.kmedical
├── domain/
│   ├── entity/          엔티티 32개 (C01–C32)
│   └── enums/           열거형 22개 (EN01–EN21 + SystemState)
│
├── dto/
│   ├── auth/            OAuthLoginRequestDTO, OAuthLoginResponseDTO
│   ├── accesslink/      AccessLinkVerifyRequestDTO, AccessLinkVerifyResponseDTO, AccessLinkCreateRequestDTO, AccessLinkDTO
│   ├── passport/        PassportUploadRequestDTO, PassportReviewRequestDTO, PassportInfoDTO
│   ├── patient/         PatientDTO, MedicalQuestionnaireDTO, EmergencyContactDTO
│   ├── quotation/       QuotationRequestDTO, QuotationCreateRequestDTO, QuotationDTO, QuotationAcceptRequestDTO
│   ├── journey/         PatientJourneyDTO, ScheduleItemDTO, ScheduleItemUpdateRequestDTO
│   ├── template/        ItineraryTemplateDTO, TemplateItemDTO
│   ├── staff/           StaffDTO, StaffAssignmentDTO, StaffAssignmentCreateRequestDTO, WorkStatusUpdateDTO, WorkProofPhotoDTO, DailyWorkReportDTO
│   ├── chat/            ConversationDTO, ChatMessageSendRequestDTO, ChatMessageDTO, AttachmentDTO
│   ├── guide/           RecoveryGuideDTO, GuideCreateRequestDTO, GuideDeliveryRequestDTO
│   ├── invoice/         InvoiceDTO, InvoiceItemDTO, InvoiceCreateRequestDTO
│   ├── agency/          AgencyDTO
│   ├── rbac/            UserRoleDTO, RoleChangeRequestDTO
│   ├── alert/           AlertDTO, AlertCreateRequestDTO
│   ├── sos/             SOSAlertRequestDTO, SOSAlertDTO
│   └── dashboard/       DashboardDataDTO
│
├── adapter/             인프라 어댑터 인터페이스 5개 (INF-A01–A05)
│   ├── OAuthAdapter
│   ├── PushAdapter
│   ├── MessengerAdapter
│   ├── TranslationAdapter
│   └── OCRAdapter
│
├── control/             Control 객체 19개 + Singleton 1개 (SRV-C01–C19)
│   ├── SystemStateRegistry  (Singleton — 시스템 상태 관리)
│   ├── AuthController       (SRV-C01, «상태 의존 컨트롤»)
│   ├── AccessLinkController (SRV-C02)
│   ├── PassportController   (SRV-C03)
│   ├── PatientController    (SRV-C04)
│   ├── AgencyController     (SRV-C05)
│   ├── QuotationController  (SRV-C06)
│   ├── TemplateController   (SRV-C07)
│   ├── JourneyController    (SRV-C08)
│   ├── StaffController      (SRV-C09)
│   ├── StaffAssignmentController (SRV-C10)
│   ├── WorkController       (SRV-C11)
│   ├── ChatController       (SRV-C12)
│   ├── AlertController      (SRV-C13)
│   ├── SOSController        (SRV-C14)
│   ├── InvoiceController    (SRV-C15)
│   ├── GuideController      (SRV-C16)
│   ├── ReportController     (SRV-C17)
│   ├── RBACController       (SRV-C18)
│   └── DashboardController  (SRV-C19)
│
├── http/                HTTP 핸들러 22개 (JDK 내장 HttpServer 바인딩)
│   ├── JsonUtil             (JSON 직렬화/파싱 유틸 — 외부 라이브러리 없음)
│   ├── BaseHandler          (공통: CORS, OPTIONS, 예외→HTTP 매핑)
│   │                        ※ IllegalArgumentException→400, ClosedDown→503, 기타 IllegalStateException→409, Exception→500
│   ├── HealthHandler        (GET /api/health)
│   ├── AuthHandler          (SRV-C01, POST /api/auth/login·logout·startup·closedown)
│   ├── AccessLinkHandler    (SRV-C02, POST /api/access-links·verify, DELETE /api/access-links/{token})
│   ├── PassportHandler      (SRV-C03, POST /api/passports/upload·review, GET /api/passports/{id})
│   ├── PatientHandler       (SRV-C04, GET/POST /api/patients/**, DELETE emergency-contacts)
│   ├── AgencyHandler        (SRV-C05, GET/POST /api/agencies, PUT /api/agencies/{id}/verify)
│   ├── QuotationHandler     (SRV-C06, /api/quotations/**)
│   ├── TemplateHandler      (SRV-C07, /api/templates/**)
│   ├── JourneyHandler       (SRV-C08, /api/journeys/**)
│   ├── StaffHandler         (SRV-C09, GET/PUT /api/staff/**)
│   ├── StaffAssignmentHandler (SRV-C10, POST/GET /api/assignments)
│   ├── WorkHandler          (SRV-C11, POST /api/work/status·photos, GET /api/work/history)
│   ├── ChatHandler          (SRV-C12, POST/GET /api/chat/**)
│   ├── AlertHandler         (SRV-C13, POST/GET /api/alerts)
│   ├── SOSHandler           (SRV-C14, POST /api/sos, PUT /api/sos/{id}/resolve, GET /api/sos/unresolved)
│   ├── InvoiceHandler       (SRV-C15, /api/invoices/**)
│   ├── GuideHandler         (SRV-C16, /api/guides/**)
│   ├── ReportHandler        (SRV-C17, POST/GET /api/reports)
│   ├── RBACHandler          (SRV-C18, POST /api/rbac/assign·revoke, GET /api/rbac/{userId}/roles·history)
│   └── DashboardHandler     (SRV-C19, GET /api/dashboard)
│
├── App.java             진입점 — 19개 컨트롤러 Manual DI, 20개 HttpServer 컨텍스트 등록, port 8080
│
└── ifo/
    ├── admin/           관리자 웹 인터페이스 14개 (IFO-A01–A14)
    │   ├── DashboardView
    │   ├── PatientListView
    │   ├── PatientDetailView
    │   ├── PassportReviewForm
    │   ├── ProposalFormView
    │   ├── JourneyTemplateEditorView
    │   ├── ScheduleEditorView
    │   ├── StaffAssignmentView
    │   ├── StaffMapMonitorView
    │   ├── ChatConsoleView
    │   ├── PostOpGuideFormView
    │   ├── InvoiceFormView
    │   ├── AgencyProfileFormView
    │   └── RBACManagementView
    │
    ├── patient/         환자 모바일 웹 인터페이스 13개 (IFO-P01–P13)
    │   ├── MagicLinkLandingView
    │   ├── OAuthLoginView
    │   ├── PassportUploadForm
    │   ├── MedicalQuestionnaireForm
    │   ├── EmergencyContactForm
    │   ├── AgencyProfileView
    │   ├── QuotationRequestForm
    │   ├── ProposalCompareView
    │   ├── ChatView
    │   ├── ItineraryView
    │   ├── NavigationView
    │   ├── PostOpGuideView
    │   └── InvoiceDownloadView
    │
    └── staff/           스태프 모바일 웹 인터페이스 10개 (IFO-S01–S10)
        ├── StaffLoginView
        ├── StaffProfileView
        ├── DailyTaskListView
        ├── PatientNoticeView
        ├── StatusUpdateView
        ├── ProofPhotoUploadForm
        ├── NavigationView
        ├── ChatView
        ├── SOSAlertView
        └── DailyReportForm
```

---

## 핵심 아키텍처 제약사항 (검증 기준)

1. **프레임워크 없음**: `java.util`, `java.time`, `java.math` 표준 라이브러리만 사용. Spring, JPA, Lombok 사용 금지.

2. **SystemStateRegistry**: Singleton 패턴. 고객 대상 모든 Control 메서드는 진입 시 `guardNotClosedDown()`을 먼저 호출해야 함.

3. **AuthController 전용 상태 변경**: `SystemStateRegistry.getInstance().setState()`를 호출하는 컨트롤러는 AuthController 하나뿐임.

4. **IFO → Control 단방향 의존**: Interface 객체는 도메인 엔티티를 직접 참조하면 안 됨.

5. **DTO 경계 원칙**: IFO ↔ Control 경계에서는 DTO만 주고받음. 엔티티 노출 금지.

6. **상태 전이 규칙**:
   - ScheduleItem: `SCHEDULED → IN_PROGRESS → COMPLETED` (역방향 불가)
   - Invoice: `DRAFT → ISSUED → CANCELLED` (`cancelInvoice`로 DRAFT 취소 불가)
   - QuotationRequest: 환자당 OPEN 최대 3건. 하나 수락 시 나머지 OPEN 요청은 모두 CLOSED 처리.
   - AccessLink: 인증 실패 5회 초과 시 15분 잠금.

7. **Control 계층의 비즈니스 규칙**:
   - EmergencyContact: 환자당 최대 2건 (PatientController)
   - WorkProofPhoto: `retentionExpiresAt = 업로드일 + 1년`. 인보이스 발행 시 `invoiceIssuedAt + 1년`으로 연장.
   - DailyWorkReport: 당일 날짜로만, 자정 전까지만 제출 가능.
   - RoleChangeHistory: 역할 부여·회수 시 항상 이력 기록 (RBACController).

---

## 실행

```bash
./build.sh   # 컴파일 → JAR → http://localhost:8080 기동
```

전체 엔드포인트 목록은 `BUILD.md` 참고.

---

## 비교 분석 보고서 수치 (L2 vs L3+L4)

`docs/L2_vs_L3L4_Analysis.md`에 전체 보고서가 작성되어 있음.

| 항목 | L2 | L3+L4 |
|------|-----|--------|
| 총 클래스 수 | 24 | 184 (명세 기반 161 + AI 임의 23) |
| 패키지 수 | 1 (default) | 6개 목적별 패키지 |
| ECB 분리 | 혼재 (단일 클래스 7가지 책임) | 분리됨 (패키지+DTO 경계) |
| C/S 아키텍처 | 없음 | 있음 (JDK HttpServer, 52개 엔드포인트) |
| 상태 관리 | 모호함 (2개 enum) | 명확함 (22개 enum + Guard 79회) |
| 동시성 처리 | 부분 (Thread.sleep 인터럽트) | 부분 (volatile 1곳, HashMap 비동기 안전 아님) |

**AI 임의 생성 23개**: App.java + BaseHandler + JsonUtil + HealthHandler + 19개 XxxHandler

---

## SA 기반 검증·NFR 리팩토링 (완료)

`com.kmedical.util` 패키지에 3개 유틸리티 클래스가 추가되었으며, 19개 Controller 전체에 적용되었다.

| 클래스 | 역할 | 주요 메서드 |
|--------|------|-------------|
| `ValidationUtil` | 입력값 정적 검증 (15종) | requireNotBlank, requireValidEmail, requireHttpsUrl, requireValidE164Phone, requireLatitude/Longitude, requireFutureDate 등 |
| `MaskingUtil` | 개인정보 마스킹 (NFR-SEC-01) | maskEmail, maskSubjectId, maskToken, maskUrl |
| `AuditLogger` | 표준 감사 로그 출력 (NFR-LOG-01/02) | log(AUDIT), warn(WARN), perf(PERF+WARN), closedDownAccess |

### NFR 구현 요약

| NFR ID | 분류 | 적용 범위 | 구현 방법 |
|--------|------|-----------|-----------|
| NFR-SEC-01 | 보안 | 감사 로그 내 개인정보 | MaskingUtil.maskEmail/maskSubjectId |
| NFR-SEC-02 | 보안 | AccessLink 토큰 생성 | SecureRandom → 64자 hex |
| NFR-SEC-03 | 보안 | 모든 외부 URL | ValidationUtil.requireHttpsUrl |
| NFR-LOG-01 | 로깅 | 11개 주요 비즈니스 이벤트 | AuditLogger.log() |
| NFR-LOG-02 | 로깅 | ClosedDown 접근 차단 | AuditLogger.closedDownAccess() |
| NFR-PERF-01 | 성능 | SOSController.triggerSOS | 500ms 임계값 + [PERF] 로그 |
| NFR-PERF-02 | 성능 | DashboardController.getDashboardData | 2000ms 임계값 + 안전 폴백 |
| NFR-PERF-03 | 성능 | ChatController.sendMessage | 1000ms 임계값 + [PERF] 로그 |

### Controller별 주요 추가 사항

- **AuthController**: AuditLogger(LOGIN/LOGOUT/STARTUP/CLOSEDOWN), MaskingUtil 적용
- **AccessLinkController**: SecureRandom 64자 hex 토큰, 잠금 해제 자동 리셋
- **PassportController**: HTTPS imageUrl 검증, OCR PERF 3000ms, PASSPORT_REVIEW 감사 로그
- **PatientController**: E.164 전화 검증, fullNameEn 패턴 검증, 연락처 최대 2건 강제
- **InvoiceController**: 금액 PositiveBigDecimal 검증, INVOICE_ISSUED/CANCELLED 감사 로그
- **WorkController**: fileUrl HTTPS 검증, takenAt 과거 날짜 검증
- **SOSController**: 좌표 범위 검증, PERF 500ms, SOS_TRIGGERED/RESOLVED 감사 로그
- **DashboardController**: PERF 2000ms, 5개 서브쿼리 개별 폴백
- **AgencyController**: licenseNumber 패턴 검증, 이메일 검증, isVerified=false 강제
- **RBACController**: ROLE_ASSIGNED/REVOKED 감사 로그
- **ChatController**: originalText 4000자 제한, PERF 1000ms
- **JourneyController**: 좌표 범위 검증, scheduledEndAt > scheduledStartAt 검증
- **QuotationController**: desiredVisitDate 미래 날짜 검증, 금액 NonNegative 검증
- **ReportController**: reportDate=today 검증, 자정 이전 제출 검증, REPORT_SUBMITTED 감사 로그
- **StaffController**: profilePhotoUrl HTTPS 검증
- **StaffAssignmentController**: STAFF_ASSIGNED 감사 로그
- **GuideController**: pdfUrl HTTPS 검증
- **TemplateController**: agencyId/createdBy 필수값 검증
- **AlertController**: ALERT_SENT/ALERT_FAILED 감사 로그

---

## 세션 재개 시

`PROGRESS_TRACKER.md`에서 현재 완료 상태를 확인하세요.
L3+L4 코드 100% / HTTP Handler 레이어 100% / PART C 명세 문서 100% / PART D 리팩토링 100% / 컴파일 0 errors 상태입니다.
누락 파일이 있으면 위 패키지 구조를 참고해 동일한 규칙(Pure Java, 프레임워크 없음, BCE 아키텍처)으로 생성하면 됩니다.
