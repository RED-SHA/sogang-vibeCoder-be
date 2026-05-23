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

## 세션 재개 시

`PROGRESS_TRACKER.md`에서 현재 완료 상태를 확인하세요.
L3+L4 코드 100%, HTTP Handler 레이어 100%, 컴파일 0 errors 상태입니다.
누락 파일이 있으면 위 패키지 구조를 참고해 동일한 규칙(Pure Java, 프레임워크 없음, BCE 아키텍처)으로 생성하면 됩니다.
