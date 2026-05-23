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

---
## 완료 현황: 100% (전체 생성 완료)
