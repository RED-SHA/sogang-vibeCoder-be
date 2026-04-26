# Use Case Description ISO 29148:2018 품질 검토 #2 — Edit Itinerary (UC-ADM-07)

> 대상: `docs/UseCaseDescription_EditItinerary.md` (개정판, 검토 #1 fix 6건 적용)
> 기준: ISO/IEC/IEEE 29148:2018 — 개별 요구사항 품질 특성 9개 + 추가 6개
> 이전 검토: `docs/UseCaseDescriptionReview_ISO29148.md`
> Iteration: 2
> 생성: 2026-04-25

---

## 1. 이전 Iteration 적용 결과 확인

| 검토 #1 항목 | 적용 여부 |
|-------------|----------|
| Singular: Step 2/5 분할, A5/A6 다단계 분할 | ✅ Step 11→15 단계로 확장, A5.1~A5.3 / A6.1~A6.4 분할 완료 |
| Conforming: Step 1 include 명시 | ✅ Step 1 = "시스템은 Authenticate User 결과를 확인한다", Dependency 4개로 확장 |
| Unambiguous: 재시도 정책·diff 포맷·락 비교 기준·"모든 관계자" | ✅ 부록 §1 §2 추가, A2 "version 정수 비교" 명시, Summary "환자와 배정된 모든 실무자" |
| Complete: A5 cancel Postcondition + degraded_sync 분리 | ✅ Postcondition 8 (cancel) 추가, 4·4a 분리 |
| Verifiable: 재시도 정책·"준비 상태" 정의 | ✅ 부록 §1 §3 추가 |
| Appropriate: 테이블명·JWT 추상화 | ✅ "일정 저장소"·"감사 로그 저장소"·"지연 발송 큐"·"유효한 인증 세션" 적용 |

---

## 2. ISO 29148:2018 9개 특성별 결과

### [1] Necessary

- **특성명** : Necessary
- **대상 필드** : 전체
- **근거** : Step 1~15 모두 도메인·NFR 직접 근거 보유. Step 7/8/9 (리비전 기록/version 증가/연관 갱신) 분할은 Singular 충족용이며 각각 검증 가능 결과를 생성하므로 불필요 단계 아님. Precondition 7개·Alternatives A1~A7·Postcondition 1~8 모두 제거 시 흐름 불완전.
- **결과**: **Pass**

### [2] Appropriate

- **특성명** : Appropriate
- **대상 필드** : Description, Postcondition
- **근거** : 본문에서 구현 수단(JWT, journey_schedule, audit_log 등) 추상화 완료. 본문은 What 수준 ("일정 저장소", "감사 로그 저장소", "유효한 인증 세션"). 구현 표준 (RFC 6902, 지수 백오프) 은 부록에 분리 → 본문 What / 부록 How 명확 구분.
- **결과**: **Pass**

### [3] Unambiguous

- **특성명** : Unambiguous
- **대상 필드** : Description, Alternatives, Summary, 부록
- **근거** :
  - Summary "환자와 배정된 모든 실무자" — 수신 범위 명확.
  - "5초 이내" — 수치 기준.
  - 재시도 정책 (1초 → 2초 → 4초, 최대 3회) — 부록 §1.
  - diff 포맷 (RFC 6902 JSON Patch) — 부록 §2.
  - "version 정수 비교 기반 낙관적 락 충돌" (A2) — 검증 기준 명시.
  - "준비 상태" 3조건 — 부록 §3.
- **결과**: **Pass**

### [4] Complete

- **특성명** : Complete
- **대상 필드** : Postcondition
- **근거** : 정상 흐름(1-6), degraded_sync 분기(4a), 검증·락·권한 실패(7), 취소(8) 모든 종료 분기에 대한 사후 상태 명시. Precondition 7개·Alternatives A1~A7 모두 본문 단계와 분기점 매핑.
- **결과**: **Pass**

### [5] Singular

- **특성명** : Singular
- **대상 필드** : Description, Alternatives
- **근거** :
  - Step 1~15 각 단계당 단일 행동/응답 (락 획득·폼 반환·리비전 기록·version 증가·assigned_staff 갱신 분리).
  - Step 5 "시작 시각, 종료 시각, 장소, 배정 실무자 식별자를 포함한 수정 필드를 제출" — 단일 제출 행위 (필드 묶음 입력은 한 행동).
  - Step 6 검증 — 단일 검증 목적.
  - A5/A6 다단계 분할 (A5.1~A5.3, A6.1~A6.4).
- **결과**: **Pass**

### [6] Feasible

- **특성명** : Feasible
- **대상 필드** : Description, Alternatives
- **근거** : 표준 백엔드 패턴(낙관적 락, message broker, push gateway, outbox, exponential backoff) 으로 구현 가능. NFR 5초 동기화는 WebSocket/SSE 표준 영역.
- **결과**: **Pass**

### [7] Verifiable

- **특성명** : Verifiable
- **대상 필드** : Description, Alternatives, Postcondition
- **근거** :
  - "5초 이내" → 측정 가능.
  - "version 단조 증가" → DB 쿼리 검증.
  - "고아 행 없음" → JOIN 쿼리 검증.
  - 재시도 정책 (부록 §1) — 횟수·간격·종료 조건 명시 → 시뮬레이션·로그 검증 가능.
  - 준비 상태 (부록 §3) — 3조건 모두 시스템 상태로 검증 가능 (락 해제/fetch 가능/입력 대기).
  - degraded_sync 플래그 + 지연 발송 큐 항목 — 응답·DB 검증 가능.
- **결과**: **Pass**

### [8] Correct

- **특성명** : Correct
- **대상 필드** : 전체
- **근거** : ADM-402 (일정 수정·동기화·푸시), NFR 실시간 동기화·데이터 정합성, RBAC, User Story §7-1 #3 (예외 통제) 모두 직접 반영.
- **결과**: **Pass**

### [9] Conforming

- **특성명** : Conforming
- **대상 필드** : 전체
- **근거** :
  - 필드 순서·콜론·공백·줄바꿈 양식 ✓
  - 단계 번호 `1.`, `A3.1.` 형식 ✓
  - Dependency 영문 UC 명칭 ✓
  - 룰 7 첫 단계 «include» ✓ (Step 1 = Authenticate User)
  - Description 본문 호출 Abstract UC 4개 모두 Dependency 등재 (Authenticate User / Audit Log Action / Synchronize Realtime State / Send Push Notification) — 호출 ↔ Dependency 1:1 일치.
- **결과**: **Pass**

---

## 3. 추가 검토 항목별 결과

### [A] Precondition 구체적 시스템 상태?

- **결과**: **Pass**
- **대상 필드** : Precondition #1~#7
- **근거** : 인증 세션·도메인 상태(ACTIVE)·동시성(편집 락 미점유)·외부 시스템 가용성(동기화 채널·푸시 게이트웨이)·저장소 쓰기 capacity·정책 적재 상태 모두 검증 가능 시스템·데이터 상태로 서술.

### [B] Description 첫 단계 «include»?

- **결과**: **Pass**
- **대상 필드** : Description Step 1
- **근거** : "시스템은 Authenticate User 결과를 확인한다." 명시.

### [C] Cancel 별도 Alternative?

- **결과**: **Pass**
- **대상 필드** : Alternatives A5
- **근거** : "에이전시 운영자가 편집을 취소하면" 별도 분기 정의됨.

### [D] 자원 회수 + 정상 종료 분리?

- **결과**: **Pass**
- **대상 필드** : Alternatives A5, A6
- **근거** :
  - A5.1 임시 페이로드 폐기 (자원 회수) / A5.2 편집 락 해제 (자원 회수) / A5.3 유스케이스 종료 (정상 종료) — 단계 분리.
  - A6.1 행위 차단 / A6.2 감사 로그 / A6.3 오류 코드 반환 / A6.4 종료 — 단계 분리.

### [E] Postcondition 시스템·데이터 상태?

- **결과**: **Pass**
- **대상 필드** : Postcondition #1~#8
- **근거** : 일정 저장소 레코드, version 단조 증가, 배정 연관 무결성, 감사 로그 영속화, 실시간 스냅샷 전달, degraded_sync 플래그, 지연 발송 큐, 푸시 알림 큐, 편집 락 해제, 실패/취소 시 무변경 — 모두 시스템·데이터 상태. UI 메시지·토스트 표현 없음.

### [F] 마지막 Description 단계 정상 상태 복귀?

- **결과**: **Pass**
- **대상 필드** : Description Step 15
- **근거** : "시스템은 대시보드 준비 상태(부록 §3 정의)로 복귀한다." 명시. 부록 §3에서 "준비 상태" = 편집 락 해제 + fetch 가능 + 입력 대기 3조건으로 검증 가능 정의.

---

## 4. 종합 결과

| 구분 | Pass | Fail |
|------|------|------|
| ISO 9개 특성 | **9** | 0 |
| 추가 6개 항목 | **6** | 0 |
| **합계** | **15 / 15** | **0** |

**총평**: ISO 29148:2018 개별 요구사항 품질 기준 **전 항목 통과**. 추가 검토 6개 항목 **전부 통과**. 검토 #1 6개 fix 모두 적정 적용됨.

---

## 5. 후속 작업

1. UC-ADM-07 Edit Itinerary Description **freeze**. WS2 시퀀스 다이어그램·API 명세 입력으로 사용 가능.
2. 동일 양식·검토 절차로 Tier 1 핵심 UC 10개 (CoreUseCases.md §2) 의 Description 작성 권장:
   - UC-ADM-04, UC-ADM-10, UC-PAT-02, UC-PAT-06, UC-PAT-09, UC-PAT-12, UC-STA-01, UC-STA-04, UC-STA-05, UC-OP-03
3. 본 검토 산출 (#2) 을 다른 UC Description 검토 시 baseline 으로 활용.
