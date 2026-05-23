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
