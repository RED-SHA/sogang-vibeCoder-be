# K-의료 관광 통합 플랫폼 — L3+L4 구현

## 아키텍처 원칙

- **Language**: Pure Java SE (표준 라이브러리만 사용, 외부 프레임워크 금지)
- **Pattern**: BCE (Boundary-Control-Entity) — Jacobson 로버스트니스 아키텍처
- **Dependency Direction**: Interface → Control → Entity (역방향 호출 금지)
- **Data Transfer**: Interface ↔ Control 계층 간 DTO 전용 사용 (Entity 직접 노출 금지)

## 계층 구조

```
Client Subsystem (IFO)
  └── ifo/admin/    IFO-A01~14  Admin Web Interface Objects
  └── ifo/patient/  IFO-P01~13  Patient Mobile Web Interface Objects
  └── ifo/staff/    IFO-S01~10  Staff Mobile Web Interface Objects

Server Subsystem
  └── control/      SRV-C01~19  Business Logic Control Objects
  └── adapter/      INF-A01~05  Infrastructure Adapter Objects
  └── domain/entity/ C01~C32   Entity Classes (L3 도메인 모델)
  └── domain/enums/  EN01~21   Enumeration Classes
  └── dto/                     Data Transfer Objects (계층 간 전달 전용)
```

## 디렉토리 구조

```
src/main/java/com/kmedical/
├── domain/
│   ├── enums/       (22 enums: EN01~EN21 + SystemState)
│   └── entity/      (32 entities: C01~C32)
├── dto/             (계층 간 데이터 전달 전용 DTO 45개)
├── adapter/         (5 adapters: INF-A01~INF-A05)
├── control/         (19 controllers: SRV-C01~SRV-C19 + SystemStateRegistry)
├── http/            (HTTP 핸들러 22개 — JDK HttpServer 바인딩)
│   ├── JsonUtil         (JSON 직렬화/파싱 유틸)
│   ├── BaseHandler      (공통 CORS·예외→HTTP 매핑)
│   ├── HealthHandler    (GET /api/health)
│   ├── AuthHandler      (SRV-C01, /api/auth/**)
│   ├── AccessLinkHandler(SRV-C02, /api/access-links/**)
│   ├── PassportHandler  (SRV-C03, /api/passports/**)
│   ├── PatientHandler   (SRV-C04, /api/patients/**)
│   ├── AgencyHandler    (SRV-C05, /api/agencies/**)
│   ├── QuotationHandler (SRV-C06, /api/quotations/**)
│   ├── TemplateHandler  (SRV-C07, /api/templates/**)
│   ├── JourneyHandler   (SRV-C08, /api/journeys/**)
│   ├── StaffHandler     (SRV-C09, /api/staff/**)
│   ├── StaffAssignmentHandler (SRV-C10, /api/assignments)
│   ├── WorkHandler      (SRV-C11, /api/work/**)
│   ├── ChatHandler      (SRV-C12, /api/chat/**)
│   ├── AlertHandler     (SRV-C13, /api/alerts)
│   ├── SOSHandler       (SRV-C14, /api/sos/**)
│   ├── InvoiceHandler   (SRV-C15, /api/invoices/**)
│   ├── GuideHandler     (SRV-C16, /api/guides/**)
│   ├── ReportHandler    (SRV-C17, /api/reports)
│   ├── RBACHandler      (SRV-C18, /api/rbac/**)
│   └── DashboardHandler (SRV-C19, /api/dashboard)
├── App.java         (진입점 — Manual DI + HttpServer 기동)
└── ifo/
    ├── admin/       (14 IFOs: IFO-A01~IFO-A14)
    ├── patient/     (13 IFOs: IFO-P01~IFO-P13)
    └── staff/       (10 IFOs: IFO-S01~IFO-S10)
```

## 실행

```bash
./build.sh          # 컴파일 → JAR → 서버 기동 (http://localhost:8080)
```

자세한 빌드/실행 방법은 `BUILD.md` 참고.

## SA 기반 검증·NFR 아키텍처

`com.kmedical.util` 패키지에 3개 유틸리티 클래스가 추가되었으며, 19개 Controller 전체에 적용된다.

| 클래스 | 역할 | 주요 메서드 |
|--------|------|-------------|
| `ValidationUtil` | 입력값 정적 검증 | requireNotBlank, requireValidEmail, requireHttpsUrl, requireValidE164Phone, requireLatitude/Longitude, requireFutureDate 등 15종 |
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

---

## 핵심 제약 사항

| # | 제약 |
|---|---|
| 1 | ScheduleItem.status: SCHEDULED → IN_PROGRESS → COMPLETED 순서만 허용 |
| 2 | QuotationRequest OPEN 상태: 환자당 최대 3건 |
| 3 | Quotation ACCEPTED: 동일 QuotationRequest에 1건만 허용 |
| 4 | AccessLink 인증 실패 5회 초과 시 15분 잠금 |
| 5 | EmergencyContact: 환자당 최대 2건 |
| 6 | StaffAssignment: 동일 ScheduleItem에 CHAUFFEUR 1명, INTERPRETER 1명 |
| 7 | Invoice ISSUED 상태: 수정 불가, CANCELLED 후 재발행만 허용 |
| 8 | WorkProofPhoto: Invoice ISSUED 기준 1년 후 자동 삭제 |
| 9 | DailyWorkReport: 업무 당일 자정 이전까지만 제출 가능 |
| 10 | 모든 금액: USD 기준 BigDecimal 타입 |
| 11 | ClosedDown 상태에서 모든 Customer UC 진입 차단 (Guard 조건) |

## System State (Operator UC)

- `SystemState.RUNNING` / `SystemState.CLOSED_DOWN`
- startUp() / closeDown() 메서드는 `AuthController`에 구현
- 모든 Control 메서드 진입부에 `guardNotClosedDown()` 호출

## 분석 문서

| 문서 | 설명 |
|------|------|
| [L2_vs_L3L4_Analysis.md](L2_vs_L3L4_Analysis.md) | L2 vs L3+L4 종합 비교 분석 보고서 — ECB 분리, C/S 아키텍처, 상태 관리, 동시성, 클래스 수 변화(24→184) 분석 |
| [C_Specification_Augmentation.md](C_Specification_Augmentation.md) | PART C 추가 명세 — 17개 클래스 용어 사전(Data Dictionary) + 3종 NFR(보안·로깅·성능) |
| [BUILD.md](BUILD.md) | 컴파일·빌드·실행 가이드 및 전체 API 엔드포인트 목록 |
| [PROGRESS_TRACKER.md](PROGRESS_TRACKER.md) | 구현 진척도 체크리스트 |
| [PROMPT_COMPLETE.md](PROMPT_COMPLETE.md) | 세션 인수인계용 컨텍스트 요약 |
| [PROMPT_COMPLETE.md](PROMPT_COMPLETE.md) | 세션 인수인계용 컨텍스트 전체 요약 (PART A~D 완료 상태) |
