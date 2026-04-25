# Core Use Cases — K-Medical Concierge OS (Step 1 후속)

> 입력: `docs/UseCaseModeling.md` (Concrete UC 38, Abstract UC 6)
> 목적: WS2 우선 구현 대상 식별 — 도메인 모델링 / 시퀀스 / API 명세의 진입점.
> 생성: 2026-04-25

---

## 1. 선정 기준

UC가 **모두** 충족해야 핵심으로 분류:

1. **MVP 가설 직접 입증** — "운영비 감소 + 환자 만족도 상승" 증명에 필수
2. **User Story §7-1 3개 흐름 중 ≥1개** 직접 구현 (신뢰 구축 / 현장 운영 / 예외 통제)
3. **Acceptance Criteria §7-2 ≥1개** 직접 충족 (다국어·2차 인증 / 5초 동기화 / 데이터 정합성)
4. **P1 필수** 우선순위
5. **Trade-off 미제외** 영역 (EMR / 결제 제외)

보조(Tier 2): 위 중 일부만 충족하지만 핵심 흐름 보완.

---

## 2. Tier 1 — 핵심 Concrete UC (11개)

| # | UC ID | Use Case | Surface | 근거 (User Story / AC / Req) | 핵심 의존 Abstract |
|---|-------|----------|---------|-----------------------------|-------------------|
| 1 | UC-ADM-07 | Edit Itinerary | Agency Back-Office | US-3 예외 통제, AC-3 데이터 정합성, ADM-402 | ABS-04, ABS-05, ABS-06 |
| 2 | UC-ADM-10 | Assign Field Staff | Agency Back-Office | US-2 매끄러운 운영, ADM-601 | ABS-05, ABS-06 |
| 3 | UC-ADM-04 | Generate Smart Proposal | Agency Back-Office | US-1 신뢰 구축, ADM-301 | — |
| 4 | UC-PAT-02 | Access via Magic Link | Patient Concierge | US-1 신뢰 구축, AC-1 다국어/2차 인증, PAT-103 | ABS-01 (+ DOB 내부) |
| 5 | UC-PAT-12 | View Live Itinerary | Patient Concierge | US-1·2 흐름, AC-1·2 다국어/실시간, PAT-601·602 | ABS-03, ABS-04 |
| 6 | UC-PAT-06 | View Assigned Staff Profile | Patient Concierge | US-1 신뢰 구축, PAT-303 | — |
| 7 | UC-PAT-09 | Approve Proposal | Patient Concierge | 케이스 활성화 funnel, PAT-403 | — |
| 8 | UC-STA-01 | Sign In via Magic Link | Staff Mobile | US-2 진입점, STA-101 | ABS-01 |
| 9 | UC-STA-05 | Update Task Status | Staff Mobile | US-2·3 핵심 입력, AC-2 5초 동기화, STA-401 | ABS-04, ABS-05 |
| 10 | UC-STA-04 | Read Patient Notice | Staff Mobile | US-2 안전·품질, STA-301 | — |
| 11 | UC-OP-03 | Manage RBAC Policy | System Operation | NFR 보안 기반, ADM-801 | ABS-06 |

**Tier 1 흐름 커버리지**:

- **신뢰 구축 funnel**: UC-ADM-04 → UC-PAT-09 → UC-PAT-02 → UC-PAT-12 + UC-PAT-06
- **현장 운영 funnel**: UC-STA-01 → UC-STA-04 → UC-STA-05 → UC-PAT-12 (실시간 갱신)
- **예외 통제 funnel**: UC-ADM-07 → ABS-04/05 → UC-PAT-12 + UC-STA-05
- **보안 기반**: UC-OP-03

---

## 3. Tier 2 — 보조 Concrete UC (8개)

| # | UC ID | Use Case | Surface | 보조 사유 |
|---|-------|----------|---------|----------|
| 1 | UC-PAT-01 | Sign Up via OAuth | Patient | 정식 회원 진입 (Magic Link 비회원 흐름이 우선) |
| 2 | UC-PAT-03 | Submit Onboarding Info | Patient | 가입 후 후속 단계 (여권 OCR + 문진) |
| 3 | UC-PAT-10 | Chat with Agency | Patient | 환자↔에이전시 양방향 채팅 (다국어) |
| 4 | UC-ADM-08 | Manage Multi-Chat Console | Agency | 다수 환자 채팅 관제 |
| 5 | UC-ADM-02 | Monitor Alert Center | Agency | 이상 이벤트 감지 (운영 가시성) |
| 6 | UC-ADM-03 | Review Patient Documents | Agency | 환자 DB 검토 + OCR 재처리 |
| 7 | UC-PAT-13 | Open Map Directions | Patient | 일정 → 지도 딥링크 (UC-PAT-12 extend) |
| 8 | UC-ADM-12 | Distribute Post-Op Guide | Agency | 사후 관리 (다국어 발송) |

---

## 4. 미선정 — 사유

| UC ID | Use Case | 미선정 사유 |
|-------|----------|------------|
| UC-OP-01, UC-OP-02 | Start Up / Shut Down System | 인프라성, 도메인 가치 입증과 무관 |
| UC-ADM-01 | View Operations Overview | 대시보드는 Tier 1 데이터 누적 후 의미 |
| UC-ADM-06 | Build Itinerary Template | UC-ADM-07로 직접 일정 생성 가능, 템플릿은 후속 효율화 |
| UC-ADM-09 | Send Translated Message | UC-ADM-08과 통합 가능 |
| UC-ADM-11 | Monitor Field Staff Status | P2 |
| UC-ADM-05, UC-ADM-13 | Manage Agency Profile / Issue Invoice | P2 |
| UC-PAT-04 | Verify Provider License | 신뢰 보조 (UC-PAT-06 사진/연락처가 우선) |
| UC-PAT-05 | Browse Hospital Portfolio | P2 |
| UC-PAT-07, UC-PAT-08 | Request Quotation / Compare Proposals | UC-ADM-04+UC-PAT-09 funnel로 단순화 가능 |
| UC-PAT-11 | Upload Medical Document | 진단/사진 첨부, MVP 후 |
| UC-PAT-14 | Read Recovery Guide | UC-ADM-12로 발송, 환자 측은 표시 일치 |
| UC-PAT-15 | Download Invoice | P2 |
| UC-STA-02 | Manage Staff Profile | 초기 1회성, 운영 흐름 외 |
| UC-STA-03 | View Daily Tasks | UC-STA-04·05 통해 진입 가능 |
| UC-STA-06, UC-STA-09, UC-STA-10 | Proof Photo / SOS / Report | P2 |
| UC-STA-07, UC-STA-08 | Open Map / Chat | extend / 보조 |

---

## 5. Acceptance Criteria 직접 충족 매트릭스

| AC (§7-2) | 충족 UC |
|----------|---------|
| AC-1 다국어 + URL 유출 시 2차 인증 | UC-PAT-02 (DOB 내부), UC-PAT-12 (ABS-03 다국어) |
| AC-2 실무자 상태 → 5초 내 환자/관리자 화면 | UC-STA-05 → ABS-04 → UC-PAT-12 |
| AC-3 일정 수정 → 모든 실무자 알림 + DB 정합성 | UC-ADM-07 → ABS-04 + ABS-05 + ABS-06 → UC-STA-05 |

---

## 6. WS2 진입 권장 순서

도메인 모델링·시퀀스·API 우선순위 (의존성 + 가설 입증 가속):

1. UC-OP-03 → RBAC·인증 기반 (보안 선결)
2. UC-PAT-02 + UC-STA-01 → Auth/Magic Link 기반 (외부 의존 정리)
3. UC-ADM-04 → UC-PAT-09 → 견적 funnel
4. UC-ADM-10 → 자원 배정
5. UC-ADM-07 → UC-STA-05 → UC-PAT-12 (Edit/Status/View 3축, ABS-04/05/06 동시 검증)
6. UC-PAT-06 + UC-STA-04 → 정보 표시 보강

이 순서로 도메인 entity·API·시퀀스를 정의하면 AC-1~3 종단 검증을 6단계 안에 마무리 가능.
