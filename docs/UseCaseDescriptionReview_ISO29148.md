# Use Case Description ISO 29148:2018 품질 검토 — Edit Itinerary (UC-ADM-07)

> 대상: `docs/UseCaseDescription_EditItinerary.md`
> 기준: ISO/IEC/IEEE 29148:2018 — 개별 요구사항 품질 특성 9개 + 추가 6개
> 생성: 2026-04-25

---

## 1. 요약 결과

| 구분 | Pass | Partial / Fail |
|------|------|---------------|
| ISO 9개 특성 | 4 (Necessary, Feasible, Correct, ✱Verifiable 일부) | 5 (Appropriate, Unambiguous, Complete, Singular, Conforming) |
| 추가 6개 항목 | 4 | 2 (Description 첫 단계 include / 자원 회수·정상 종료 분리) |

**총평**: 핵심 의도·구조는 적합하나, "How 누출", 모호 표현, 단일 행동 위반, 룰 7 첫 단계 include 미명시 등 5개 항목 보완 필요.

---

## 2. ISO 29148:2018 9개 특성별 결과

### [1] Necessary

- **특성명** : Necessary
- **대상 필드** : 전체 (Precondition, Description, Alternatives, Postcondition)
- **근거** : Precondition 6개·Description 11단계·Alternatives A1~A7·Postcondition 7개 모두 제거 시 UC 흐름이 불완전해짐. 락 획득(Step 2), 락 해제(Step 9), 감사 로그(Step 6), 동기화(Step 7), 푸시(Step 8)는 ADM-402 + NFR 데이터 정합성·실시간 동기화의 직접 근거.
- **결과**: **Pass**

### [2] Appropriate

- **특성명** : Appropriate
- **대상 필드** : Description Step 5, Step 6, A3.1 / Postcondition #1~#3
- **근거** : "journey_schedule 테이블", "audit_log 항목", "outbox 테이블", "JWT" 등 구체 구현 수단(How)이 노출됨. UC Description은 What 수준 추상화가 원칙.
- **수정 제안** : 테이블명 → "일정 저장소(schedule store)", "감사 로그 저장소", "지연 발송 큐", JWT → "유효한 인증 세션"으로 추상화. 단 NFR 추적성 위해 부록 트레이스에서는 구현 수단 명시 가능.
- **결과**: **Fail**

### [3] Unambiguous

- **특성명** : Unambiguous
- **대상 필드** : Description Step 6, A3, A4 / Precondition
- **근거** :
  - "변경 전후 diff" — diff 형식(JSON Patch / RFC 6902 / 자체 포맷) 미명시.
  - "재시도 정책에 따라" (A4) — 정책 본문 부재 → 해석자별 차이.
  - "낙관적 락 버전 충돌" (A2) — 충돌 감지 기준(version 필드 비교 등) 미명시.
  - "모든 관계자" (Summary) — 환자 + 실무자 범위 명시 부족.
- **수정 제안** : diff 형식·재시도 정책·락 비교 기준을 Precondition 또는 부록 용어 정의에 추가. Summary "모든 관계자" → "배정된 실무자 및 해당 환자".
- **결과**: **Fail**

### [4] Complete

- **특성명** : Complete
- **대상 필드** : Postcondition (A5 cancel 분리 누락)
- **근거** : Postcondition #7 "On failure" 가 A1·A2·A6·A7만 다룸. A5 (운영자 취소)의 사후 상태(임시 데이터 폐기·락 해제·DB 무변경) 별도 명시 없음. A3 degraded_sync 의 Postcondition은 #4에 포함되어 있으나 outbox 항목·DB 무결성 관계가 분리 표기되지 않음.
- **수정 제안** : Postcondition 항목 추가 — `8. 취소 종료 시 (A5): 임시 페이로드 폐기, 편집 락 해제, journey_schedule 무변경.` 그리고 #4 안의 degraded_sync 분기를 #4a/#4b로 분리.
- **결과**: **Fail**

### [5] Singular

- **특성명** : Singular
- **대상 필드** : Description Step 2, Step 5
- **근거** :
  - Step 2: "편집 락을 획득하고 ... 폼과 함께 반환한다" — 락 획득 + 응답 반환 = 2개 행동.
  - Step 5: "리비전을 기록하고 version을 증가시키며 assigned_staff 연관을 갱신한다" — 3개 행동 ("and" 묶음).
  - Step 4 ("스키마, 비즈니스 규칙, 형제 항목 중복 검증")는 단일 검증 목적 → 허용.
- **수정 제안** : Step 2 → 2.1 락 획득 / 2.2 폼 반환. Step 5 → 5.1 리비전 기록 / 5.2 version 증가 / 5.3 assigned_staff 갱신. 단계 번호 재정렬.
- **결과**: **Fail**

### [6] Feasible

- **특성명** : Feasible
- **대상 필드** : Description, Alternatives
- **근거** : 모든 단계가 표준 백엔드 패턴(낙관적 락, message broker publish, push gateway HTTP, outbox pattern, exponential backoff)으로 구현 가능. 5초 이내 동기화는 WebSocket/SSE로 검증된 영역.
- **결과**: **Pass**

### [7] Verifiable

- **특성명** : Verifiable
- **대상 필드** : Description Step 7, Step 11 / Alternatives A4
- **근거** :
  - "5초 이내" (Step 7) → 측정 가능 ✓.
  - "version 단조 증가" (Postcondition #1) → DB 쿼리 검증 가능 ✓.
  - "재시도 정책" (A4) → 정책 본문 부재로 PASS/FAIL 기준 정의 불가.
  - "대시보드 준비 상태로 복귀" (Step 11) → "준비 상태" 명세 없음 → 검증 모호.
- **수정 제안** : 재시도 정책(횟수·간격·종료 조건) 명시. "준비 상태" 정의(예: "편집 락 해제 + 신규 fetch 가능") 추가.
- **결과**: **Fail (부분)**

### [8] Correct

- **특성명** : Correct
- **대상 필드** : 전체
- **근거** : ADM-402(일정 수정 + 5초 반영 + 푸시), NFR 실시간 동기화, NFR 데이터 정합성, RBAC(룰 5), User Story §7-1 #3(예외 통제) 모두 직접 반영. 이해관계자 요구와 일치.
- **결과**: **Pass**

### [9] Conforming

- **특성명** : Conforming
- **대상 필드** : Description Step 1
- **근거** :
  - 필드 순서·콜론·공백·줄바꿈 ✓
  - Dependency UC 영문 명칭 ✓
  - Alternatives `A1.`·`A3.1.` 표기 ✓
  - **위반**: 룰 7 "첫 단계에 «include» Abstract UC 호출을 명시한다" — 현재 Step 1 = "운영자가 항목을 선택" (Actor 액션). Include 호출은 Step 6/7/8에 분산.
- **수정 제안** : 양식 룰 우선이라면 Step 0 또는 Step 1을 "시스템은 Authenticate User 결과를 확인한다" 또는 "시스템은 RBAC 검증을 위해 Audit Log Action 의 사전 컨텍스트를 준비한다"로 추가. 또는 룰 7 자체를 "첫 번째 시스템 응답 단계"로 완화 해석. 모델링 정합성 우선이면 룰 7 해석 노트를 부록에 추가.
- **결과**: **Fail (양식 위반)**

---

## 3. 추가 검토 항목별 결과

### [A] Precondition 구체적 시스템 상태?

- **결과**: **Pass**
- **대상 필드** : Precondition #1~#6
- **근거** : 인증(JWT+Role), 도메인 상태(ACTIVE 케이스), 동시성(편집 락 미점유), 외부 시스템 가용성(Sync Bus, Push Gateway), 저장소 capacity 분리 명시. "None"·모호 표현 없음.

### [B] Description 첫 단계 «include»?

- **결과**: **Fail**
- **대상 필드** : Description Step 1
- **근거** : 위 [9] Conforming 와 동일 사유.
- **수정 제안** : 위 [9] 동일.

### [C] Cancel 별도 Alternative?

- **결과**: **Pass**
- **대상 필드** : Alternatives A5
- **근거** : "에이전시 운영자가 편집을 취소하면" 별도 분기로 정의됨.

### [D] 자원 회수 + 정상 종료 분리?

- **결과**: **Fail (부분)**
- **대상 필드** : Alternatives A5
- **근거** : A5 한 줄에 "임시 페이로드 폐기 + 편집 락 해제 + 유스케이스 종료"가 묶여 있음. Singular 위반과 동일 패턴. A6도 "감사 로그 + 오류 반환 + 종료" 묶음.
- **수정 제안** : A5 → A5.1 임시 페이로드 폐기 / A5.2 편집 락 해제 / A5.3 유스케이스 종료. A6도 동일 분할.

### [E] Postcondition 시스템·데이터 상태?

- **결과**: **Pass**
- **대상 필드** : Postcondition 전체
- **근거** : DB 레코드, 연관, 큐, 락 상태로 서술. UI 메시지·토스트 표현 없음.

### [F] 마지막 Description 단계 정상 상태 복귀?

- **결과**: **Pass (해석 모호 → 보완 권장)**
- **대상 필드** : Description Step 11
- **근거** : "대시보드 준비 상태로 복귀" 명시. 단 "준비 상태"의 운영적 정의 부재 → Verifiable 측면 약함.
- **수정 제안** : "준비 상태" = "편집 락 해제 완료 + 갱신 데이터 신규 fetch 가능 + 다음 작업 입력 대기"로 부록 정의 추가.

---

## 4. 우선 수정 권장 (Iteration #2 대비)

| 우선순위 | 수정 항목 | 근거 |
|---------|----------|------|
| 1 | Description Step 2/5 분할 (Singular) + A5/A6 분할 (자원 회수 분리) | 룰 7 + ISO Singular 위반 다발 |
| 2 | Description Step 0 또는 1 에 «include» 명시 (Conforming) | 양식 룰 7 직접 위반 |
| 3 | "재시도 정책" 본문 정의 + A4 명시 (Unambiguous, Verifiable) | 검증 불가 표현 제거 |
| 4 | Postcondition 에 A5 cancel 종료 상태 별도 항목 추가 (Complete) | 실패 분기 일부 누락 |
| 5 | 테이블명·JWT 등 구현 수단 추상화 (Appropriate) | What vs How 분리 |
| 6 | "변경 전후 diff" 포맷 정의 / "준비 상태" 정의 부록 추가 (Unambiguous, Verifiable) | 해석 일관성 |

---

## 5. 결론

- **Pass 항목**: Necessary, Feasible, Correct, Precondition 구체성, Cancel 분리, Postcondition 상태 서술, 정상 상태 복귀 (조건부)
- **Fail / Partial 항목**: Appropriate, Unambiguous, Complete, Singular, Conforming, Verifiable, Description 첫 단계 include, 자원 회수 분리

위 6개 우선 수정 적용 시 ISO 29148:2018 개별 요구사항 품질 기준 전 항목 충족 가능.
