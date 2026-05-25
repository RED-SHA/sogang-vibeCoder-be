# PROGRESS TRACKER — K-의료 관광 L3+L4 코드 생성

## 전체 클래스 목록 및 진척도 (161)

### Enum Classes (21)
- [x] Language (EN01)
- [x] OAuthProviderType (EN02)
- [x] OnboardingStatus (EN03)
- [x] StaffAvailability (EN04)
- [x] UserRoleName (EN05)
- [x] PassportReviewStatus (EN06)
- [x] RequiredServiceType (EN07)
- [x] QuotationRequestStatus (EN08)
- [x] QuotationStatus (EN09)
- [x] JourneyStatus (EN10)
- [x] ScheduleItemType (EN11)
- [x] ScheduleItemStatus (EN12)
- [x] StaffRole (EN13)
- [x] WorkStatus (EN14)
- [x] ConversationType (EN15)
- [x] AttachmentType (EN16)
- [x] InvoiceStatus (EN17)
- [x] AccessLinkType (EN18)
- [x] AlertType (EN19)
- [x] AlertChannel (EN20)
- [x] AlertStatus (EN21)

### System State Enum (1)
- [x] SystemState

### Entity Classes (32)
- [x] User (C01) — abstract
- [x] Admin (C02)
- [x] Patient (C03)
- [x] Staff (C04) — abstract
- [x] Chauffeur (C05)
- [x] Interpreter (C06)
- [x] Agency (C07)
- [x] PassportInfo (C08)
- [x] MedicalQuestionnaire (C09)
- [x] EmergencyContact (C10)
- [x] QuotationRequest (C11)
- [x] Quotation (C12)
- [x] ItineraryTemplate (C13)
- [x] PatientJourney (C14)
- [x] ScheduleItem (C15)
- [x] StaffAssignment (C16)
- [x] WorkStatusUpdate (C17)
- [x] WorkProofPhoto (C18)
- [x] Conversation (C19)
- [x] ChatMessage (C20)
- [x] Attachment (C21)
- [x] RecoveryGuide (C22)
- [x] GuideDeliveryHistory (C23)
- [x] Invoice (C24)
- [x] InvoiceItem (C25)
- [x] AccessLink (C26)
- [x] Alert (C27)
- [x] EmergencyAlert (C28)
- [x] DailyWorkReport (C29)
- [x] UserRole (C30)
- [x] RoleChangeHistory (C31)
- [x] TemplateItem (C32)

### DTO Classes (45)
- [x] OAuthLoginRequestDTO
- [x] OAuthLoginResponseDTO
- [x] AccessLinkVerifyRequestDTO
- [x] AccessLinkVerifyResponseDTO
- [x] AccessLinkCreateRequestDTO
- [x] AccessLinkDTO
- [x] PassportUploadRequestDTO
- [x] PassportReviewRequestDTO
- [x] PassportInfoDTO
- [x] PatientDTO
- [x] MedicalQuestionnaireDTO
- [x] EmergencyContactDTO
- [x] QuotationRequestDTO
- [x] QuotationDTO
- [x] QuotationCreateRequestDTO
- [x] QuotationAcceptRequestDTO
- [x] PatientJourneyDTO
- [x] ScheduleItemDTO
- [x] ScheduleItemUpdateRequestDTO
- [x] ItineraryTemplateDTO
- [x] TemplateItemDTO
- [x] StaffDTO
- [x] StaffAssignmentDTO
- [x] StaffAssignmentCreateRequestDTO
- [x] WorkStatusUpdateDTO
- [x] WorkProofPhotoDTO
- [x] DailyWorkReportDTO
- [x] ConversationDTO
- [x] ChatMessageDTO
- [x] ChatMessageSendRequestDTO
- [x] AttachmentDTO
- [x] RecoveryGuideDTO
- [x] GuideCreateRequestDTO
- [x] GuideDeliveryRequestDTO
- [x] InvoiceDTO
- [x] InvoiceItemDTO
- [x] InvoiceCreateRequestDTO
- [x] AgencyDTO
- [x] UserRoleDTO
- [x] RoleChangeRequestDTO
- [x] AlertDTO
- [x] AlertCreateRequestDTO
- [x] SOSAlertDTO
- [x] SOSAlertRequestDTO
- [x] DashboardDataDTO

### Infrastructure Adapter Objects (5)
- [x] OAuthAdapter (INF-A01)
- [x] PushAdapter (INF-A02)
- [x] MessengerAdapter (INF-A03)
- [x] TranslationAdapter (INF-A04)
- [x] OCRAdapter (INF-A05)

### Control Objects (19 + Singleton 1)
- [x] SystemStateRegistry (Singleton — 시스템 상태 관리, 모든 Control이 참조)
- [x] AuthController (SRV-C01)
- [x] AccessLinkController (SRV-C02)
- [x] PassportController (SRV-C03)
- [x] PatientController (SRV-C04)
- [x] AgencyController (SRV-C05)
- [x] QuotationController (SRV-C06)
- [x] TemplateController (SRV-C07)
- [x] JourneyController (SRV-C08)
- [x] StaffController (SRV-C09)
- [x] StaffAssignmentController (SRV-C10)
- [x] WorkController (SRV-C11)
- [x] ChatController (SRV-C12)
- [x] AlertController (SRV-C13)
- [x] SOSController (SRV-C14)
- [x] InvoiceController (SRV-C15)
- [x] GuideController (SRV-C16)
- [x] ReportController (SRV-C17)
- [x] RBACController (SRV-C18)
- [x] DashboardController (SRV-C19)

### Interface Objects — Admin Web (14)
- [x] DashboardView (IFO-A01)
- [x] PatientListView (IFO-A02)
- [x] PatientDetailView (IFO-A03)
- [x] PassportReviewForm (IFO-A04)
- [x] ProposalFormView (IFO-A05)
- [x] JourneyTemplateEditorView (IFO-A06)
- [x] ScheduleEditorView (IFO-A07)
- [x] StaffAssignmentView (IFO-A08)
- [x] StaffMapMonitorView (IFO-A09)
- [x] ChatConsoleView (IFO-A10)
- [x] PostOpGuideFormView (IFO-A11)
- [x] InvoiceFormView (IFO-A12)
- [x] AgencyProfileFormView (IFO-A13)
- [x] RBACManagementView (IFO-A14)

### Interface Objects — Patient Mobile Web (13)
- [x] MagicLinkLandingView (IFO-P01)
- [x] OAuthLoginView (IFO-P02)
- [x] PassportUploadForm (IFO-P03)
- [x] MedicalQuestionnaireForm (IFO-P04)
- [x] EmergencyContactForm (IFO-P05)
- [x] AgencyProfileView (IFO-P06)
- [x] QuotationRequestForm (IFO-P07)
- [x] ProposalCompareView (IFO-P08)
- [x] ChatView (IFO-P09)
- [x] ItineraryView (IFO-P10)
- [x] NavigationView (IFO-P11)
- [x] PostOpGuideView (IFO-P12)
- [x] InvoiceDownloadView (IFO-P13)

### Interface Objects — Staff Mobile Web (10)
- [x] StaffLoginView (IFO-S01)
- [x] StaffProfileView (IFO-S02)
- [x] DailyTaskListView (IFO-S03)
- [x] PatientNoticeView (IFO-S04)
- [x] StatusUpdateView (IFO-S05)
- [x] ProofPhotoUploadForm (IFO-S06)
- [x] NavigationView (IFO-S07)
- [x] ChatView (IFO-S08)
- [x] SOSAlertView (IFO-S09)
- [x] DailyReportForm (IFO-S10)

### HTTP Handlers (com.kmedical.http) — 신규 생성 대상
- [x] JsonUtil (JSON 직렬화/역직렬화 유틸)
- [x] BaseHandler (공통 요청/응답 처리, CORS, 예외→HTTP 매핑)
- [x] HealthHandler (GET /api/health)
- [x] QuotationHandler (SRV-C06 매핑, 5개 엔드포인트)
- [x] JourneyHandler (SRV-C08 매핑, 4개 엔드포인트)
- [x] AuthHandler (SRV-C01 매핑, POST /api/auth/login·logout·startup·closedown)
- [x] AccessLinkHandler (SRV-C02 매핑, POST /api/access-links·verify, DELETE /api/access-links/{token})
- [x] PassportHandler (SRV-C03 매핑, POST /api/passports/upload·review, GET /api/passports/{id})
- [x] PatientHandler (SRV-C04 매핑, GET/POST /api/patients/**, DELETE /api/patients/{id}/emergency-contacts/{cid})
- [x] AgencyHandler (SRV-C05 매핑, GET/POST /api/agencies, PUT /api/agencies/{id}/verify)
- [x] TemplateHandler (SRV-C07 매핑, GET/POST /api/templates/**)
- [x] StaffHandler (SRV-C09 매핑, GET/PUT /api/staff/**)
- [x] StaffAssignmentHandler (SRV-C10 매핑, POST/GET /api/assignments)
- [x] WorkHandler (SRV-C11 매핑, POST /api/work/status·photos, GET /api/work/history)
- [x] ChatHandler (SRV-C12 매핑, POST/GET /api/chat/**)
- [x] AlertHandler (SRV-C13 매핑, POST /api/alerts, GET /api/alerts)
- [x] SOSHandler (SRV-C14 매핑, POST /api/sos, PUT /api/sos/{id}/resolve, GET /api/sos/unresolved)
- [x] InvoiceHandler (SRV-C15 매핑, GET/POST /api/invoices/**)
- [x] GuideHandler (SRV-C16 매핑, GET/POST /api/guides/**)
- [x] ReportHandler (SRV-C17 매핑, POST/GET /api/reports)
- [x] RBACHandler (SRV-C18 매핑, POST /api/rbac/assign·revoke, GET /api/rbac/{userId}/**)
- [x] DashboardHandler (SRV-C19 매핑, GET /api/dashboard)
- [x] App.java (전체 컨트롤러 Manual DI + HttpServer 셋업, 20개 컨텍스트 등록)

---

### 분석 보고서 작업 (L2 vs L3+L4 비교)
- [x] L2 클래스 카운트 및 역할 분류 (24개 파일 분석 완료)
- [x] L3+L4 클래스 분류 (명세 기반 161개 / AI 임의 23개 / Total 184개)
- [x] ECB 분리 분석 (L2 혼재 → L3+L4 패키지 수준 명확 분리)
- [x] 아키텍처(C/S) 분석 (L2 없음 → L3+L4 HttpServer 기반 C/S 분리)
- [x] 상태 관리 분석 (L2 모호함 → L3+L4 enum State Machine + Guard 79회)
- [x] 동시성 처리 분석 (L2 부분 / L3+L4 부분)
- [x] 마크다운 문서 생성 → docs/L2_vs_L3L4_Analysis.md

---

### PART C. Specification Augmentation (추가 명세 작성)
- [x] 코드베이스 스캔 (32개 Entity + 45개 DTO 필드 구조 파악)
- [x] Data Dictionary 정의 (17개 클래스 × 속성별 타입·제약·정규식)
- [x] NFR 정의 (보안·로깅·성능 3종)
- [x] Markdown 문서 포맷팅 완결 → docs/C_Specification_Augmentation.md
- [x] README.md 갱신 (C 문서 링크 추가)
- [x] NEXT_AGENT_PROMPT.md 생성

---

### PART D. SA 기반 리팩토링 (Validation + NFR 코드 구현)

#### D-1. 신규 유틸리티 클래스 (com.kmedical.util)
- [x] ValidationUtil — 정규식·범위·필수값 검증 메서드 15종
- [x] MaskingUtil — 이메일·토큰·URL 마스킹 메서드
- [x] AuditLogger — [AUDIT]/[WARN]/[PERF] 표준 포맷 로그 출력

#### D-2. Control 계층 리팩토링
- [x] AuthController — ConcurrentHashMap, 입력 검증, AuditLogger(LOGIN/LOGOUT/STARTUP/CLOSEDOWN)
- [x] PatientController — ConcurrentHashMap, E.164 전화 검증, fullNameEn 검증
- [x] InvoiceController — ConcurrentHashMap, 금액 검증, AuditLogger(ISSUED/CANCELLED)
- [x] AccessLinkController — ConcurrentHashMap, SecureRandom 토큰, 잠금 메시지 개선
- [x] WorkController — ConcurrentHashMap, takenAt 과거 검증, fileUrl HTTPS 검증
- [x] SOSController — ConcurrentHashMap, 좌표 범위 검증, PERF 로깅(500ms), AuditLogger
- [x] PassportController — ConcurrentHashMap, HTTPS imageUrl 검증, AuditLogger(PASSPORT_REVIEW), PERF 로깅(3000ms)
- [x] DashboardController — PERF 로깅(2000ms), 안전 폴백 반환
- [x] AgencyController — ConcurrentHashMap, 라이선스번호 검증, 이메일 검증
- [x] AlertController — ConcurrentHashMap
- [x] ChatController — ConcurrentHashMap, 메시지 길이 검증, PERF 로깅(1000ms)
- [x] GuideController — ConcurrentHashMap
- [x] JourneyController — ConcurrentHashMap, 좌표 검증, 시간 순서 검증
- [x] QuotationController — ConcurrentHashMap, OPEN 건수 검증, 날짜 검증
- [x] RBACController — ConcurrentHashMap, AuditLogger(ROLE_ASSIGNED/REVOKED)
- [x] ReportController — ConcurrentHashMap, 당일 날짜 검증, 자정 이전 검증
- [x] StaffController — ConcurrentHashMap, HTTPS URL 검증
- [x] StaffAssignmentController — ConcurrentHashMap
- [x] TemplateController — ConcurrentHashMap

#### D-3. guardNotClosedDown 로깅 통합
- [x] 모든 Controller의 guardNotClosedDown()에 AuditLogger.closedDownAccess() 연결

---
## 완료 현황: L3+L4 코드 100% / HTTP Handler 레이어 100% / 컴파일 0 errors / 분석 보고서 생성 완료 / C 명세 문서 작성 완료 / D 리팩토링 100%
