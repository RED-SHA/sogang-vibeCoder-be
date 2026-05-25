# L2 vs L3+L4 비교 분석

## 1. 한눈에 보는 구조 변화

| 항목 | L2 | L3+L4 | 비고 |
|---|---|---|---|
| 생성된 클래스 수 | 24개 | 184개 | 7.7배 증가 |
| 명세 기반 클래스 수 | 24개 | 161개 | 설계 문서에 명시된 객체 |
| AI 임의 생성 클래스 수 | 0개 | 23개 | 명세에 없으나 실행에 필요해 추가, App.java 1 + HTTP 핸들러 22개 |
| ECB 분리 | 혼재 | 분리됨 | L2는 단일 클래스에 혼재 → L3+L4는 패키지 단위로 분리 |
| 아키텍처(C/S) 분리 | 없음 | 있음 | L2는 HTTP 처리 없음 → L3+L4는 JDK HttpServer 기반 REST API |
| 상태 관리 명확성 | 모호함 | 명확함 | L2는 2개 enum (ErrorCode, PatchOp) → L3+L4는 22개 상태 enum + 8종 상태 전이 규칙 + Guard 79회 호출 |
| 동시성 처리 | 부분 | 부분 | L2와 동일 수준, 개선 필요 | 

---

## 2. 코드 파일 수

### L2 — 24개

핵심 동작(`waitForOperatorSelection`, `waitForSubmissionOrCancel` 등)은 `return null`로 비어 있어 실제 실행되지 않는 골격 수준 코드다.

| 역할 | 클래스 | 개수 |
|---|---|---|
| **합계** | | **24** |
| 핵심 흐름 제어 | EditItinerary (전체 흐름 담당) | 1 |
| 인터페이스 정의 | AlertCenter, ItineraryRepository 등 경계 객체 | 6 |
| 보조 동작 | 인증, 알림, 동기화, 감사 로그 | 4 |
| 데이터 객체 | ItineraryItem, EditPayload 등 | 4 |
| 예외 | LockHeldException 등 | 5 |
| Enum | ErrorCode, PatchOp | 2 |
| 유틸리티 | RetryPolicy | 1 |

### L3+L4 — 184개

| 구분 | 패키지 | 개수 |
|---|---|---|
| **합계** | | **184** |
| 데이터 구조 (Entity) | `domain/entity` | 32 |
| 상태 상수 (Enum) | `domain/enums` | 22 |
| 계층 간 데이터 전달 (DTO) | `dto/**` | 45 |
| 외부 시스템 연결 (Adapter) | `adapter` | 5 |
| 비즈니스 로직 (Control) | `control` | 20 |
| 화면 진입점 (IFO) | `ifo/admin`, `ifo/patient`, `ifo/staff` | 37 |
| **명세 기반 소계** | | **161** |
| HTTP 핸들러 (AI 추가) | `http` | 22 |
| 앱 진입점 (AI 추가) | 루트 | 1 |
| **AI 임의 생성 소계** | | **23** |

---

## 3. 역할 분리 (ECB)

L2는 전체 24개 파일이 하나의 `default package`에 위치하며, `EditItinerary` 단일 클래스가 인증·입력 처리·검증·저장·알림·응답 생성까지 모두 담당한다.
Boundary 인터페이스는 존재하지만 실제 흐름 제어는 하나의 클래스에 집중되어 있어, 역할 분리가 구조적으로 강제되지 않는다.

반면 L3+L4는 기능과 역할 기준으로 패키지를 분리하고, Controller 19개로 책임을 분산했다.
또한 IFO → Control → Entity 단방향 구조와 DTO 계층을 적용해 계층 간 의존성과 Entity 노출을 제한했다.

| 항목 | L2 | L3+L4 |
|---|---|---|
| 패키지 구조 | 단일 default package | 6개 목적별 패키지 |
| Entity 노출 | 없음 (인터페이스 기반) | DTO 계층으로 Entity 완전 격리 |
| Control 클래스 수 | 1개 (EditItinerary) | 19개 (기능별 독립 Controller) |
| Boundary 클래스 수 | 6개 인터페이스 (구현 없음) | 37개 IFO + 5개 Adapter |
| 의존 방향 강제 | 불가 (패키지 없음) | `IFO → Control → Entity` 단방향 규칙 |

---

## 4. 서버 구조 (C/S 분리)

L2는 Java 메서드 직접 호출로만 동작하는 구조라 외부에서 요청을 받을 방법이 없다.
L3+L4는 JDK에 내장된 `com.sun.net.httpserver.HttpServer`를 사용해 Spring 같은 외부 라이브러리 없이도 실제 HTTP 서버로 동작한다.

| 항목 | L2 | L3+L4 |
|---|---|---|
| HTTP 서버 | 없음 | JDK 내장 HttpServer (포트 8080) |
| API 엔드포인트 | 0개 | 52개 |
| 실제 기동 가능 여부 | 불가 | `./build.sh` 한 줄로 기동 |
| 클라이언트-서버 분리 | 없음 | IFO(화면) ↔ http Handler ↔ Control 3단계 분리 |
| 외부 프레임워크 | 없음 | 없음 (Pure Java 유지) |

---

## 5. 상태 관리

| 항목 | L2 | L3+L4 |
|---|---|---|
| 상태 Enum 수 | 2개 (오류 코드, 편집 연산자) | 22개 (도메인 전반 커버) |
| 상태 전이 규칙 | 없음 | 8종 규칙, 역방향 전이 시 예외 발생 |
| 시스템 운영 상태 | 개념 없음 | RUNNING / CLOSED_DOWN, Guard 79회 |
| Guard (접근 차단) | 없음 | 영업 종료 시 고객 기능 전체 차단 |

### L3+L4의 상태 변경 규칙

| Enum | 허용 전이 | 비고 |
|---|---|---|
| ScheduleItem 상태 | `SCHEDULED → IN_PROGRESS → COMPLETED` | 역방향 불가 |
| Invoice 상태 | `DRAFT → ISSUED → CANCELLED` | ISSUED 후 수정 불가 |
| QuotationRequest 상태 | `OPEN → CLOSED / EXPIRED` | 환자당 OPEN 최대 3건 |
| Quotation 상태 | `DRAFT → ISSUED → ACCEPTED` | 동일 요청에 수락 1건만 허용 |
| Journey 상태 | `SCHEDULED → IN_PROGRESS → COMPLETED` | |
| Onboarding 상태 | `PENDING → SUBMITTED → APPROVED` | |
| Passport 검토 상태 | `PENDING → APPROVED / REJECTED` | |
| 시스템 상태 | `RUNNING ↔ CLOSED_DOWN` | AuthController만 변경 가능 |

---

## 6. 동시성 처리

L3+L4는 여러 요청을 동시에 처리하는 구조(스레드 풀)를 갖췄지만, 내부 데이터 저장소(HashMap)는 동시 접근에 안전하지 않다. 실제 서비스 전에 반드시 해결해야 할 부분이다.

| 항목 | L2 | L3+L4 |
|---|---|---|
| Thread 인터럽트 처리 | 올바르게 처리 (`interrupt()` 복원) | 해당 없음 |
| volatile 사용 | 없음 | SystemStateRegistry.state 1곳 |
| 동시 요청 처리 | 구조 자체 없음 | 스레드 풀 8개로 병렬 처리 |
| 공유 저장소 안전성 | 해당 없음 | HashMap 19개 — 비동기 안전 아님 (미해결) |

---

## 부록. 잔존 개선 필요 사항

현재 구현은 설계 아키텍처를 검증하는 **인메모리 프로토타입** 수준이다. 위 네 항목을 순서대로 해결하면 실제 서비스 수준의 코드가 된다.

| 항목 | 현재 문제 | 해결 방향 |
|---|---|---|
| 동시성 | HashMap 동시 접근 시 데이터 손실 가능 | ConcurrentHashMap 교체 또는 DB 레이어 도입 |
| 데이터 영속성 | 서버 재시작 시 모든 데이터 사라짐 | 실제 DB 연동 (H2, PostgreSQL 등) |
| 인증 세션 | 서버 재시작 시 로그인 세션 소실 | DB 기반 세션 관리 |
| 입력 검증 | 필수 필드 누락 시 NullPointerException 발생 가능 | Handler 레이어에서 필수값 사전 검증 추가 |

---

## 부록. AI 임의 생성 클래스 목록 (23개)

명세 문서에는 없으나, 코드가 실제로 실행되기 위해 AI가 추가로 작성한 클래스들이다.

| 클래스 | 역할 |
|---|---|
| `App` | 서버 기동, 19개 Controller 조립(Manual DI), ShutdownHook 등록 |
| `BaseHandler` | 모든 Handler 공통 처리: CORS 헤더, 예외→HTTP 상태코드 변환 |
| `JsonUtil` | 외부 라이브러리 없이 Map ↔ JSON 변환 |
| `HealthHandler` | GET /api/health — 서버 상태 확인용 |
| `AuthHandler` | SRV-C01 (인증) HTTP 연결 |
| `AccessLinkHandler` | SRV-C02 (접근 링크) HTTP 연결 |
| `PassportHandler` | SRV-C03 (여권 검토) HTTP 연결 |
| `PatientHandler` | SRV-C04 (환자 관리) HTTP 연결 |
| `AgencyHandler` | SRV-C05 (에이전시) HTTP 연결 |
| `QuotationHandler` | SRV-C06 (견적) HTTP 연결 |
| `TemplateHandler` | SRV-C07 (일정 템플릿) HTTP 연결 |
| `JourneyHandler` | SRV-C08 (여정) HTTP 연결 |
| `StaffHandler` | SRV-C09 (스태프 프로필) HTTP 연결 |
| `StaffAssignmentHandler` | SRV-C10 (스태프 배정) HTTP 연결 |
| `WorkHandler` | SRV-C11 (업무 기록) HTTP 연결 |
| `ChatHandler` | SRV-C12 (채팅) HTTP 연결 |
| `AlertHandler` | SRV-C13 (알림) HTTP 연결 |
| `SOSHandler` | SRV-C14 (긴급 SOS) HTTP 연결 |
| `InvoiceHandler` | SRV-C15 (인보이스) HTTP 연결 |
| `GuideHandler` | SRV-C16 (회복 안내) HTTP 연결 |
| `ReportHandler` | SRV-C17 (업무 일지) HTTP 연결 |
| `RBACHandler` | SRV-C18 (권한 관리) HTTP 연결 |
| `DashboardHandler` | SRV-C19 (대시보드) HTTP 연결 |

*총 23개 = 1개 App.java + 22개 HTTP 핸들러
