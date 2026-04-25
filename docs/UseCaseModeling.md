# WS1 Use Case Modeling — K-Medical Concierge OS

> Step 1-1 Use Case Modeling 결과 산출물.
> 입력: `docs/requirements.md`, 규칙: `docs/UseCaseModelingRules.md`.
> 다음 단계: Use Case Diagram 생성.

---

## 1. Actor 식별

### 1-1. Human Actor

| Actor | 설명 | 인증 경로 | 근거 Req |
|-------|------|----------|---------|
| **Operator** | 시스템 lifecycle / RBAC 정책 담당 | 시스템 콘솔 | ADM-801 (Master 역할) |
| **Master Admin** | 에이전시 최고 권한자, RBAC 정책 적용 대상 | Email/SSO | ADM-801 |
| **Agency Admin** | 일반 운영 관리자 (대시보드·환자·일정·채팅) | Email/SSO | ADM-101 외 |
| **Registered Patient** | OAuth 정식 가입 환자 | OAuth (Google/Apple/WhatsApp) | PAT-101 |
| **Guest Patient** | 매직 링크 + 생년월일 2차 인증 비회원 | Magic Link + DOB | PAT-103 |
| **Field Staff** | 기사·통역사 (동일 인증 경로·권한) | Email/SMS Magic Link 또는 SNS | STA-101 |

> Driver와 Interpreter는 인증 경로·권한 동일 → 단일 `Field Staff` Actor로 통합.
> Master Admin과 Agency Admin은 RBAC 권한 차이 → 분리.

### 1-2. External System Actor (`<<external>>`)

| Actor | 사용처 | 근거 |
|-------|-------|------|
| **OAuth Provider** | 환자 소셜 로그인 | PAT-101 |
| **Magic Link Provider** | 환자/실무자 매직 링크 발송 | PAT-103, STA-101 |
| **OCR Service** | 여권 정보 자동 추출 | PAT-201, ADM-201 |
| **Translation Engine** | 자동 번역 (대화·일정·가이드) | ADM-502, PAT-501, STA-601, NFR 다국어 |
| **Realtime Sync Bus** | WebSocket/SSE 5초 동기화 | NFR 실시간 동기화, ADM-402, PAT-602 |
| **Push Notification Gateway** | Web Push/이메일/모바일 푸시 | ADM-102, PAT-603, STA-202 |
| **Encrypted Storage** | 의료 사진·문서 암호화 저장 | PAT-502, NFR 프라이버시 |
| **Map Service** | Google/Apple Maps 딥링크 | PAT-604, STA-501 |

---

## 2. Use Case 식별

### 2-1. Surface 그룹

- **System Operation** — Operator 전용, 시스템 lifecycle
- **Agency Back-Office** — Master Admin / Agency Admin
- **Patient Concierge** — Registered Patient / Guest Patient
- **Staff Mobile** — Field Staff

### 2-2. Concrete Use Case

#### System Operation

| UC ID | Use Case | Primary Actor | Req ID | 비고 |
|-------|---------|--------------|--------|------|
| UC-OP-01 | Start Up System | Operator | — | 시스템 부팅 |
| UC-OP-02 | Shut Down System | Operator | — | 안전 종료 |
| UC-OP-03 | Manage RBAC Policy | Operator, Master Admin | ADM-801 | 역할·기능 접근 제어 |

#### Agency Back-Office

| UC ID | Use Case | Primary Actor | Req ID | 비고 |
|-------|---------|--------------|--------|------|
| UC-ADM-01 | View Operations Overview | Agency Admin | ADM-101 | KPI 요약 |
| UC-ADM-02 | Monitor Alert Center | Agency Admin | ADM-102 | 이상 이벤트 |
| UC-ADM-03 | Review Patient Documents | Agency Admin | ADM-201 | 여권·문진·알레르기 검토 |
| UC-ADM-04 | Generate Smart Proposal | Agency Admin | ADM-301 | 견적서 생성 |
| UC-ADM-05 | Manage Agency Profile | Master Admin | ADM-302 | P2 |
| UC-ADM-06 | Build Itinerary Template | Agency Admin | ADM-401 | 템플릿 CMS |
| UC-ADM-07 | Edit Itinerary | Agency Admin | ADM-402 | 실시간 동기화·푸시 포함 |
| UC-ADM-08 | Manage Multi-Chat Console | Agency Admin | ADM-501 | 다중 채팅 관제 |
| UC-ADM-09 | Send Translated Message | Agency Admin | ADM-502 | 번역 채팅 송신 |
| UC-ADM-10 | Assign Field Staff | Agency Admin | ADM-601 | 자동·원클릭 배정 |
| UC-ADM-11 | Monitor Field Staff Status | Agency Admin | ADM-602 | P2, 위치/상태 |
| UC-ADM-12 | Distribute Post-Op Guide | Agency Admin | ADM-701 | 다국어 발송 |
| UC-ADM-13 | Issue Invoice | Agency Admin | ADM-702 | P2 |

#### Patient Concierge

| UC ID | Use Case | Primary Actor | Req ID | 비고 |
|-------|---------|--------------|--------|------|
| UC-PAT-01 | Sign Up via OAuth | Registered Patient | PAT-101, PAT-102 | 동의는 흡수 |
| UC-PAT-02 | Access via Magic Link | Guest Patient | PAT-103 | 2차 인증 필수 |
| UC-PAT-03 | Submit Onboarding Info | Registered Patient | PAT-201, PAT-202, PAT-203 | 여권+문진+긴급연락처 통합 |
| UC-PAT-04 | Verify Provider License | Registered Patient, Guest Patient | PAT-301 | 신뢰 정보 |
| UC-PAT-05 | Browse Hospital Portfolio | Registered Patient | PAT-302 | P2 |
| UC-PAT-06 | View Assigned Staff Profile | Registered Patient, Guest Patient | PAT-303 | 사진·언어·연락처 |
| UC-PAT-07 | Request Quotation | Registered Patient | PAT-401 | 견적 요청 |
| UC-PAT-08 | Compare Proposals | Registered Patient | PAT-402 | 다수 견적 비교 |
| UC-PAT-09 | Approve Proposal | Registered Patient | PAT-403 | 케이스 활성화 |
| UC-PAT-10 | Chat with Agency | Registered Patient | PAT-501, PAT-503 | 채팅 + 시스템 알림 통합 |
| UC-PAT-11 | Upload Medical Document | Registered Patient | PAT-502 | 암호화 첨부 |
| UC-PAT-12 | View Live Itinerary | Registered Patient, Guest Patient | PAT-601, PAT-602 | 라이브 + 5초 동기화 통합 |
| UC-PAT-13 | Open Map Directions | Registered Patient, Guest Patient | PAT-604 | 지도 딥링크 |
| UC-PAT-14 | Read Recovery Guide | Registered Patient | PAT-701 | 다국어 가이드 |
| UC-PAT-15 | Download Invoice | Registered Patient | PAT-702 | P2 |

#### Staff Mobile

| UC ID | Use Case | Primary Actor | Req ID | 비고 |
|-------|---------|--------------|--------|------|
| UC-STA-01 | Sign In via Magic Link | Field Staff | STA-101 | Magic Link/SNS |
| UC-STA-02 | Manage Staff Profile | Field Staff | STA-102 | 사진·언어·차량 |
| UC-STA-03 | View Daily Tasks | Field Staff | STA-201 | To-do 카드 |
| UC-STA-04 | Read Patient Notice | Field Staff | STA-301 | 특이사항 |
| UC-STA-05 | Update Task Status | Field Staff | STA-401 | 원터치 상태 |
| UC-STA-06 | Upload Proof Photo | Field Staff | STA-402 | P2, 픽업·드롭 증빙 |
| UC-STA-07 | Open Map Directions | Field Staff | STA-501 | 지도 딥링크 |
| UC-STA-08 | Chat with Agency or Patient | Field Staff | STA-601 | 번역 채팅 |
| UC-STA-09 | Send SOS Alert | Field Staff | STA-602 | P2 |
| UC-STA-10 | Generate End-of-Day Report | Field Staff | STA-701 | P2 |

### 2-3. UC 통합 결정 근거

- `PAT-601 + PAT-602` → `UC-PAT-12 View Live Itinerary` 1개로 통합 (동일 Actor, 동일 목표). 5초 동기화는 `ABS-04`로 흡수.
- `PAT-201 + PAT-202 + PAT-203` → `UC-PAT-03 Submit Onboarding Info` 1개 (온보딩 완료라는 단일 goal).
- `PAT-501 + PAT-503` → `UC-PAT-10 Chat with Agency` 1개 (채팅창 내 메시지/알림).
- `PAT-102` (T&C 동의) → 단독 UC 없음, `UC-PAT-01`에 흡수 (가입 step).
- `PAT-603`, `STA-202` (푸시 수신) → 단독 UC 미생성, `ABS-05 Send Push Notification`로 흡수.
- `ADM-402` 일정 변경 시 동기화·푸시·감사로깅 → `UC-ADM-07`이 `ABS-04, ABS-05, ABS-06` 동시 include.

### 2-4. UC 미생성 항목 (Trade-off 제외)

- EMR 연동 — 명시적 trade-off 제외.
- 글로벌 결제/환전 — MVP 배제 (PAT-702, ADM-702는 인보이스 발행/다운로드만 유지).

---

## 3. Abstract Use Case

> 단일 Concrete UC만 호출하면 Abstract로 분리하지 않고 내부 단계로 흡수. 아래는 모두 다수 Concrete UC가 참조.

| UC ID | Abstract UC | 의존 External | 호출하는 Concrete UC |
|-------|-------------|--------------|---------------------|
| ABS-01 | «abstract» Authenticate User | OAuth Provider, Magic Link Provider | UC-PAT-01, UC-PAT-02, UC-STA-01 |
| ABS-03 | «abstract» Translate Content | Translation Engine | UC-ADM-09, UC-PAT-10, UC-STA-08, UC-PAT-12, UC-PAT-14, UC-ADM-12 |
| ABS-04 | «abstract» Synchronize Realtime State | Realtime Sync Bus | UC-ADM-07, UC-STA-05, UC-PAT-12, UC-ADM-11 |
| ABS-05 | «abstract» Send Push Notification | Push Notification Gateway | UC-ADM-02, UC-ADM-07, UC-ADM-10, UC-ADM-12, UC-STA-05, UC-STA-09 |
| ABS-06 | «abstract» Audit Log Action | — | UC-ADM-07, UC-ADM-10, UC-OP-03 |
| ABS-07 | «abstract» Encrypt File Storage | Encrypted Storage | UC-PAT-11, UC-STA-06, UC-ADM-03 |

> ABS-02 (Verify Second Factor)는 단일 Concrete(UC-PAT-02)만 호출 → 룰 3에 따라 흡수, UC-PAT-02 내부 단계로 표현 (생년월일 2차 인증).

---

## 4. UC 관계

### 4-1. «include» (항상 실행되는 공통 흐름)

| Concrete UC | → include | Abstract UC |
|------------|-----------|-------------|
| UC-PAT-01 Sign Up via OAuth | → | ABS-01 |
| UC-PAT-02 Access via Magic Link | → | ABS-01 (DOB 2차 인증은 내부 단계로 흡수) |
| UC-STA-01 Sign In via Magic Link | → | ABS-01 |
| UC-ADM-07 Edit Itinerary | → | ABS-04, ABS-05, ABS-06 |
| UC-ADM-10 Assign Field Staff | → | ABS-05, ABS-06 |
| UC-ADM-09 Send Translated Message | → | ABS-03 |
| UC-PAT-10 Chat with Agency | → | ABS-03 |
| UC-STA-08 Chat with Agency or Patient | → | ABS-03 |
| UC-PAT-12 View Live Itinerary | → | ABS-03, ABS-04 |
| UC-STA-05 Update Task Status | → | ABS-04, ABS-05 |
| UC-ADM-11 Monitor Field Staff Status | → | ABS-04 |
| UC-PAT-11 Upload Medical Document | → | ABS-07 |
| UC-STA-06 Upload Proof Photo | → | ABS-07 |
| UC-ADM-03 Review Patient Documents | → | ABS-07 |
| UC-PAT-14 Read Recovery Guide | → | ABS-03 |
| UC-ADM-12 Distribute Post-Op Guide | → | ABS-03, ABS-05 |
| UC-STA-09 Send SOS Alert | → | ABS-05 |
| UC-OP-03 Manage RBAC Policy | → | ABS-06 |

### 4-2. «extend» (조건부 흐름)

| Extension UC | → extend | Base UC | 조건 |
|-------------|----------|---------|------|
| UC-STA-06 Upload Proof Photo | → | UC-STA-05 Update Task Status | 픽업·드롭 시점에 사진 첨부 선택 |
| UC-PAT-13 Open Map Directions | → | UC-PAT-12 View Live Itinerary | 환자가 일정 내 장소 탭 시 |
| UC-STA-07 Open Map Directions | → | UC-STA-03 View Daily Tasks | Staff가 업무 카드 주소 탭 시 |
| UC-PAT-05 Browse Hospital Portfolio | → | UC-PAT-04 Verify Provider License | 라이선스 확인 후 추가 신뢰 자료 열람 (P2) |

### 4-3. External System 의존 (UC → External)

| UC | → 외부 호출 | External Actor |
|----|------------|----------------|
| UC-PAT-03 Submit Onboarding Info | → | OCR Service (여권 OCR) |
| UC-ADM-03 Review Patient Documents | → | OCR Service (재추출) |
| UC-PAT-13 Open Map Directions | → | Map Service |
| UC-STA-07 Open Map Directions | → | Map Service |
| ABS-01 | → | OAuth Provider, Magic Link Provider |
| ABS-03 | → | Translation Engine |
| ABS-04 | → | Realtime Sync Bus |
| ABS-05 | → | Push Notification Gateway |
| ABS-07 | → | Encrypted Storage |

### 4-4. Actor ↔ UC 연결 검증

- 모든 Human Actor가 ≥1 UC와 연결됨 ✓
- 모든 External Actor가 Abstract UC 또는 Concrete UC와 연결됨 ✓
- 고립 UC 없음 (모든 Concrete UC는 Actor 또는 «extend»로 결합) ✓

---

## 5. 추적성 매트릭스 (Req ID ↔ UC ID)

### 5-1. 관리자 Side

| Req ID | UC ID |
|--------|-------|
| ADM-101 | UC-ADM-01 |
| ADM-102 | UC-ADM-02 |
| ADM-201 | UC-ADM-03 |
| ADM-301 | UC-ADM-04 |
| ADM-302 | UC-ADM-05 |
| ADM-401 | UC-ADM-06 |
| ADM-402 | UC-ADM-07 |
| ADM-501 | UC-ADM-08 |
| ADM-502 | UC-ADM-09 (+ ABS-03) |
| ADM-601 | UC-ADM-10 |
| ADM-602 | UC-ADM-11 |
| ADM-701 | UC-ADM-12 |
| ADM-702 | UC-ADM-13 |
| ADM-801 | UC-OP-03 |

### 5-2. 환자 Side

| Req ID | UC ID |
|--------|-------|
| PAT-101 | UC-PAT-01 |
| PAT-102 | UC-PAT-01 (흡수) |
| PAT-103 | UC-PAT-02 (DOB 2차 인증 내부 흡수) |
| PAT-201 | UC-PAT-03 |
| PAT-202 | UC-PAT-03 |
| PAT-203 | UC-PAT-03 |
| PAT-301 | UC-PAT-04 |
| PAT-302 | UC-PAT-05 |
| PAT-303 | UC-PAT-06 |
| PAT-401 | UC-PAT-07 |
| PAT-402 | UC-PAT-08 |
| PAT-403 | UC-PAT-09 |
| PAT-501 | UC-PAT-10 (+ ABS-03) |
| PAT-502 | UC-PAT-11 (+ ABS-07) |
| PAT-503 | UC-PAT-10 |
| PAT-601 | UC-PAT-12 |
| PAT-602 | UC-PAT-12 (+ ABS-04) |
| PAT-603 | ABS-05 (흡수) |
| PAT-604 | UC-PAT-13 |
| PAT-701 | UC-PAT-14 (+ ABS-03) |
| PAT-702 | UC-PAT-15 |

### 5-3. 실무자 Side

| Req ID | UC ID |
|--------|-------|
| STA-101 | UC-STA-01 (+ ABS-01) |
| STA-102 | UC-STA-02 |
| STA-201 | UC-STA-03 |
| STA-202 | ABS-05 (흡수) |
| STA-301 | UC-STA-04 |
| STA-401 | UC-STA-05 (+ ABS-04, ABS-05) |
| STA-402 | UC-STA-06 (+ ABS-07) |
| STA-501 | UC-STA-07 |
| STA-601 | UC-STA-08 (+ ABS-03) |
| STA-602 | UC-STA-09 (+ ABS-05) |
| STA-701 | UC-STA-10 |

### 5-4. NFR 매핑

| NFR 영역 | Abstract UC / 처리 |
|---------|-------------------|
| 다국어 | ABS-03 Translate Content |
| 보안 (2차 인증) | UC-PAT-02 내부 단계 (생년월일 검증) |
| 실시간 동기화 (5초) | ABS-04 Synchronize Realtime State |
| 데이터 정합성 | UC-ADM-07 + ABS-04 + ABS-05 + ABS-06 조합 |
| 프라이버시 (암호화) | ABS-07 Encrypt File Storage |

---

## 6. MVP 우선순위 분류 (Diagram 표기용)

### P1 필수 (MVP 포함)
UC-OP-01, UC-OP-02, UC-OP-03, UC-ADM-01, UC-ADM-02, UC-ADM-03, UC-ADM-04, UC-ADM-06, UC-ADM-07, UC-ADM-08, UC-ADM-09, UC-ADM-10, UC-ADM-12, UC-PAT-01, UC-PAT-02, UC-PAT-03, UC-PAT-04, UC-PAT-06, UC-PAT-07, UC-PAT-08, UC-PAT-09, UC-PAT-10, UC-PAT-11, UC-PAT-12, UC-PAT-13, UC-PAT-14, UC-STA-01, UC-STA-02, UC-STA-03, UC-STA-04, UC-STA-05, UC-STA-07, UC-STA-08, ABS-01~07

### P2 (MVP 제외 또는 후속)
UC-ADM-05, UC-ADM-11, UC-ADM-13, UC-PAT-05, UC-PAT-15, UC-STA-06, UC-STA-09, UC-STA-10

> Diagram 생성 시 P2는 점선 외곽 또는 별도 색상으로 구분 표기 권장.

---

## 7. Diagram 생성 입력 요약

다음 단계(Use Case Diagram) 입력 체크리스트:

- [x] System Boundary 명: `K-Medical Concierge OS`
- [x] Surface 4개 package: System Operation / Agency Back-Office / Patient Concierge / Staff Mobile
- [x] Human Actor 6개 (좌측 배치)
- [x] External System Actor 8개 (우측 배치, `<<external>>`)
- [x] Concrete UC 38개
- [x] Abstract UC 6개 (`«abstract»`)
- [x] include / extend / external dependency 관계 정의 완료
- [x] UC ID ↔ Req ID 추적성 확보
