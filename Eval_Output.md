# L1 vs L2 생성 코드 비교 평가

- **L1 산출물**: `Output_L1/` (Use Case Diagram 기반 골격 코드)
- **L2 산출물**: `Output_L2/` (Use Case Description 리뷰본 기반 상세 코드)
- **대상 유스케이스**: UC-ADM-07 *Edit Itinerary*

---

## 0. 산출 파일 인벤토리

| 구분 | L1 (`Output_L1/`) | L2 (`Output_L2/`) |
|---|---|---|
| 파일 수 | 7 | 24 |
| 클래스 | `AgencyAdmin`, `EditItinerary`, `AuditLogAction`(abstract), `SendPushNotification`(abstract), `SynchronizeRealtimeState`(abstract), `PushNotificationGateway`, `RealtimeSyncBus` | `AgencyOperator`, `EditItinerary`, `EditItineraryResponse`, `EditPayload`, `ItineraryItem`, `JsonPatchEntry`, `AuthenticateUser`(+`AuthenticationResult`), `AuditLogAction`, `SynchronizeRealtimeState`, `SendPushNotification`, `RetryPolicy` |
| 인터페이스 | 0 | `ItineraryRepository`, `AuditLogRepository`, `AlertCenter`, `DelayedDispatchQueue`, `RealtimeSyncBus`, `PushNotificationGateway` |
| Enum | 0 | `ErrorCode`, `PatchOp` |
| Exception | 0 | `EditCancelledException`, `LockHeldException`, `OptimisticLockConflictException`, `PermissionDeniedException`, `ValidationException` |

---

## 1. 클래스 수 비교

| 항목 | L1 | L2 |
|---|---|---|
| 총 타입 수 | 7 | 24 (+`AuthenticationResult` 중첩 포함 시 25) |
| 증가 배수 | 1× | 약 3.4× |

- **L1**: 다이어그램에 등장한 액터 / 유스케이스 / 외부 시스템만 1:1 매핑. 도메인·예외·VO 없음.
- **L2**: 도메인 객체(`ItineraryItem`, `EditPayload`, `JsonPatchEntry`), 응답 객체(`EditItineraryResponse`), 정책(`RetryPolicy`), 예외 5종, 저장소·게이트웨이 인터페이스 4종, enum 2종이 추가됨.

**평가**: Description의 단계·대안·부록까지 흡수하면서 책임이 정확히 분리됨. 클래스 수 폭증은 과설계가 아니라 명세 흡수의 결과.

---

## 2. Actor 구분 반영 여부

| 항목 | L1 | L2 |
|---|---|---|
| 주요 액터 클래스 | `AgencyAdmin` (필드 없음, TODO) | `AgencyOperator(operatorId)` — 식별자 보유 |
| 외부 시스템 액터 | `PushNotificationGateway`, `RealtimeSyncBus` (빈 클래스, TODO) | 동일 이름의 **인터페이스**로 승격 + 구체 메서드(`dispatch`, `publish`) 정의 |
| 보조 액터(2차) | 없음 | `AlertCenter`, `DelayedDispatchQueue`, `AuditLogRepository`, `ItineraryRepository` 인터페이스로 표현 |

**평가**: L2는 액터가 단순 클래스가 아닌 **경계 인터페이스**로 식별되어 어댑터 교체 가능. 이름도 `AgencyAdmin → AgencyOperator`로 Description 어휘와 일치.

---

## 3. Abstract UC가 독립 클래스로 분리되었는가

| Abstract UC | L1 | L2 |
|---|---|---|
| Authenticate User | ❌ 분리 안 됨 | ✅ `AuthenticateUser` 클래스 |
| Audit Log Action | ✅ `AuditLogAction` (abstract, run() 비어있음) | ✅ `AuditLogAction` (run / runRejection 두 시그니처) |
| Synchronize Realtime State | ✅ abstract, run() 빈 메서드 | ✅ `publish(ItineraryItem) → boolean`, 재시도 내장 |
| Send Push Notification | ✅ abstract, run() 빈 메서드 | ✅ `queue(staffIds, endpoint) → boolean`, 재시도 내장 |

**평가**: L1은 "abstract 키워드만 붙고 본문 비어있음"으로 추출의 형태만 흉내. L2는 별도 클래스로 분리되었으면서 **실제 호출 시그니처와 결과 타입**까지 명세에 맞게 정의됨.

---

## 4. `«include»` 관계가 코드에서 필드/의존으로 표현되었는가

**L1**: `EditItinerary`가 3개 abstract UC를 생성자 주입 필드로 보유. `execute()` 안에서 `synchronizeRealtimeState.run()` 등으로 순차 호출. include 관계는 표현되지만 매개변수·반환 없음.

**L2**: `EditItinerary`가 4개 include 대상(`AuthenticateUser`, `AuditLogAction`, `SynchronizeRealtimeState`, `SendPushNotification`) + 보조 의존 3개를 생성자 주입. 각 호출이 **인자(operatorId, diff, snapshot 등)와 반환값**을 가져 제어 흐름에 통합됨. 호출 라인마다 `// Step N` / `// A3.1` 주석으로 §4 트레이스가 코드에 박혀있음.

**평가**: L2가 include의 의미를 "데이터 흐름이 있는 의존"으로 정확히 표현.

---

## 5. Description의 각 단계가 메서드로 구현되었는가

**L1**: `execute()` 한 메서드. 본문은 TODO + include 호출 3줄. Description 단계 매핑 자체가 없음 (다이어그램 기반이라 단계가 없음).

**L2**: `EditItinerary.execute()` + `runLockedFlow()`에서 **Step 1~15** 가 모두 코드 라인으로 등장하고 행마다 `// Step N` 주석. 보조 메서드 매핑:

| Description Step | L2 구현 위치 |
|---|---|
| Step 1 인증 | `authenticateUser.run(...)` |
| Step 2 항목 선택 | `waitForOperatorSelection(...)` |
| Step 3 락 획득 | `itineraryRepository.acquireLock(...)` |
| Step 4 폼 반환 | `itineraryRepository.loadEditableForm(...)` |
| Step 5 제출 | `waitForSubmissionOrCancel(...)` |
| Step 6 검증 | `validate(payload, before)` |
| Step 7 신리비전 기록 | `itineraryRepository.writeNewRevision(...)` |
| Step 8 version+1 | `updated.withIncrementedVersion()` |
| Step 9 실무자 갱신 | `updated.withAssignedStaff(...)` |
| Step 10 감사 | `auditLogAction.run(...)` |
| Step 11 동기화 | `synchronizeRealtimeState.publish(...)` |
| Step 12 푸시 | `sendPushNotification.queue(...)` |
| Step 13 락 해제 | `itineraryRepository.releaseLock(...)` |
| Step 14 응답 | `EditItineraryResponse.success(...)` |
| Step 15 대시보드 복귀 | TODO 주석으로 명시 |

**평가**: L2는 단계-메서드 1:1 매핑이 거의 완벽. 명세상 미정의 부분만 TODO로 보존.

---

## 6. Alternatives 예외가 Exception 클래스로 구현되었는가

| Alt | L1 | L2 |
|---|---|---|
| A1 검증 실패 | ❌ | ✅ `ValidationException(fieldErrors)` |
| A2 낙관적 락 충돌 | ❌ | ✅ `OptimisticLockConflictException(expected, actual)` |
| A5 편집 취소 | ❌ | ✅ `EditCancelledException` |
| A6 권한 거부 | ❌ | ✅ `PermissionDeniedException(reason)` (단, 본 흐름은 예외 throw 대신 결과 객체 분기로 처리) |
| A7 락 보유 | ❌ | ✅ `LockHeldException(lockHolderId)` |

**평가**: L2는 모든 Alternative에 대해 전용 예외 + **상태 보존 필드**(holder, version, fieldErrors 등)를 부여.

---

## 7. Cancel이 별도로 처리되었는가

**L1**: 취소 개념 없음.

**L2**: `EditCancelledException` 정의 + `execute()` 내 `try { waitForSubmissionOrCancel(...) } catch (EditCancelledException ce)` 블록에서 A5.1(payload 폐기) → A5.2(락 해제) → A5.3(종료) 순으로 명세대로 분기. 단, A5.3 응답 형태가 명세에 없어 `return null` + TODO 주석으로 표시 — 정직하게 누락 표시.

---

## 8. Precondition이 가드 조건으로 구현되었는가

**L1**: 가드 없음.

**L2**:
- 인증: `if (!auth.isWritePermissionGranted())` 가드 → A6 분기.
- 락: `acquireLock()` 실패 throw → catch에서 즉시 종료.
- 검증: `validate()` throw → 재시도 루프.

단, "사용자가 활성 세션을 갖고 있다", "RBAC 정책 정의됨" 같은 **암묵 Precondition**은 별도 assertion으로는 구현되지 않고 의존성 존재로만 표현됨.

**평가**: 명세에 명시적인 가드는 모두 코드화됨. 환경/세션류 가정은 미반영(명세에도 의존성으로만 기술됨).

---

## 9. Postcondition이 코드에 반영되었는가

| Postcondition | L1 | L2 |
|---|---|---|
| 1. version+1, 새 리비전 | ❌ | ✅ Description Step 8 / Step 7 |
| 2. 구독 클라이언트 동기화 (또는 degraded_sync) | ❌ | ✅ `degradedSync` 플래그를 응답에 포함 |
| 3. 감사 로그(operator_id, diff, ISO 8601) | ❌ | ✅ `auditLogAction.run(operatorId, diff, Instant.now())` |
| 4. 알림 분기(성공/큐 등록/실패) | ❌ | ✅ Description Step 12 + A4.1 + `AlertCenter.recordFailure` |
| 5. 락 해제 | ❌ | ✅ Description Step 13 + A5.2 |

**평가**: L2는 5개 Postcondition 모두 실제 코드 경로로 반영.

---

## 10. 자원 회수와 정상 종료가 구분되었는가

**L1**: 자원 개념 없음.

**L2**:
- 정상 종료(Description Step 13): `releaseLock(...)` 호출 후 응답 생성·반환.
- 취소 경로(A5.2): catch 블록에서 `releaseLock(...)` 후 종료.
- 권한 거부(A6) / 락 미획득(A7): 락을 잡기 전 종료이므로 회수 불필요 — 코드 흐름이 이를 반영.

**미흡**: `try / finally`로 락 해제를 묶지는 않아 검증 루프 내 unchecked 예외 발생 시 누수 가능성 존재. 명세 기반 충실도는 충분하나 방어적 자원관리는 한 단계 부족.

---

## 11. 도메인 객체(엔티티/VO)가 추출되었는가

**L1**: 도메인 객체 0건. 모든 호출이 무인자.

**L2**:
- 엔티티: `ItineraryItem` (id, version, startTime, endTime, location, assignedStaffIds — 불변, `withIncrementedVersion`/`withAssignedStaff` 카피온라이트).
- VO/입력: `EditPayload`, `JsonPatchEntry`, `AuthenticationResult`, `EditItineraryResponse`.
- 액터 식별자: `AgencyOperator(operatorId)`.

**평가**: 명세에서 추출 가능한 모든 도메인 개념이 분리되었고 **불변 + 정적 팩토리 + 카피온라이트** 패턴으로 일관됨.

---

## 13. 문자열 리터럴·매직 넘버 대신 enum / 명명 상수를 사용하였는가

**L1**: 해당 없음(상수 사용처가 없음).

**L2**:
- `ErrorCode { LOCK_HELD, PERMISSION_DENIED, VALIDATION_FAILED, OPTIMISTIC_LOCK_CONFLICT }` — 응답 분기를 enum으로.
- `PatchOp { REPLACE, ADD, REMOVE }` — JSON Patch op를 enum으로.
- `RetryPolicy.INITIAL_DELAY_SECONDS=1`, `BACKOFF_MULTIPLIER=2`, `MAX_ATTEMPTS=3` — 부록 §1 수치를 명명 상수로.
- `degradedSync`는 boolean 필드(매직 스트링 아님).

**잔존 매직 스트링**: 예외 메시지(`"lock_held"`, `"validation_failed"` 등)는 문자열로 그대로 — `ErrorCode.name()` 재사용 가능. 사소한 개선 여지.

---

## 14. 파라미터·반환 타입이 구체 도메인 타입인가

**L1**: 모든 메서드 시그니처가 `void run()` / `void execute()`. 도메인 타입 0개.

**L2**: 시그니처 예시
- `EditItinerary.execute(AgencyOperator) → EditItineraryResponse`
- `ItineraryRepository.writeNewRevision(String, EditPayload, int) → ItineraryItem`
- `SynchronizeRealtimeState.publish(ItineraryItem) → boolean`
- `AuditLogAction.run(String operatorId, List<JsonPatchEntry> diff, Instant timestamp)`

**미흡**: `EditItineraryResponse.updatedItineraryView`가 `Object`로 남음 — Description Step 14 "갱신된 일정 뷰" 표현이 명세에 미정이라 TODO. `String operatorId` 같은 식별자가 원시 타입(전용 VO 미생성).

**평가**: 핵심 흐름은 도메인 타입 시그니처. 일부 표현 객체만 약타입으로 잔존.

---

## 15. 생성자 주입·인터페이스 의존을 통해 단위 테스트 가능 수준인가

**L1**: 생성자 주입은 형식적으로 존재하나, 주입 대상이 abstract 클래스이고 메서드가 빈 `void run()` 라서 **stub 외에 테스트할 행위 자체가 없음**.

**L2**:
- `EditItinerary`가 7개 의존을 모두 생성자 주입.
- 핵심 의존이 `interface`(저장소, 게이트웨이, AlertCenter, DelayedDispatchQueue) → Mockito stub 즉시 가능.
- 실제 행위 클래스(`AuthenticateUser`, `AuditLogAction`, `SynchronizeRealtimeState`, `SendPushNotification`)는 클래스 주입 — Mockito mock 가능하나, 추후 인터페이스로 추출 시 더 깔끔.
- 분기 검증 시나리오(권한 거부, 락 충돌, 검증 실패, 동기화 실패, 푸시 실패, 취소)가 모두 분리된 메서드 호출 + 예외로 표현되어 단위 테스트 작성 직선적.

**평가**: L2는 즉시 단위 테스트 작성 가능. L1은 테스트 대상 자체가 부재.

---

## 16. 도메인 어휘가 클래스/인터페이스명에 잘 반영되었는가

| 명세 어휘 | L1 | L2 |
|---|---|---|
| 에이전시 운영자 | `AgencyAdmin` (다이어그램 어휘) | `AgencyOperator` (Description 어휘) |
| 일정 항목 | — | `ItineraryItem` |
| 편집 페이로드 | — | `EditPayload` |
| 편집 락 | — | `ItineraryRepository.acquireLock/releaseLock` |
| 낙관적 락 충돌 | — | `OptimisticLockConflictException` |
| 지연 발송 큐 | — | `DelayedDispatchQueue` |
| 알림 센터 | — | `AlertCenter` |
| 감사 로그 | `AuditLogAction` | `AuditLogAction` + `AuditLogRepository` |
| degraded_sync | — | `EditItineraryResponse.degradedSync` |
| JSON Patch | — | `JsonPatchEntry`, `PatchOp` |
| 재시도 정책 | — | `RetryPolicy` |

**평가**: L2는 명세 한국어 어휘 → 영어 클래스명 매핑이 일관되며 누락 거의 없음. 이름만 보고도 Description의 어느 절을 가리키는지 식별 가능.

---

## 종합 평가표

| # | 항목 | L1 | L2 |
|---|---|---|---|
| 1 | 클래스 수 | 7 | 24 |
| 2 | Actor 구분 | △ (이름만) | ◎ (인터페이스 + 식별자) |
| 3 | Abstract UC 분리 | △ (껍데기) | ◎ (시그니처 완비) |
| 4 | `«include»` 표현 | ○ (필드/호출만) | ◎ (인자·반환·트레이스 주석) |
| 5 | Description 단계 → 메서드 | ✕ | ◎ (1:1 매핑) |
| 6 | Alternatives → Exception | ✕ | ◎ (5종) |
| 7 | Cancel 분리 | ✕ | ◎ |
| 8 | Precondition 가드 | ✕ | ○ (명시 가드 모두) |
| 9 | Postcondition 반영 | ✕ | ◎ (5/5) |
| 10 | 자원 회수/정상 종료 | ✕ | ○ (try-finally는 미사용) |
| 11 | 도메인 객체 추출 | ✕ | ◎ |
| 13 | enum/명명 상수 | — | ◎ (소소한 메시지 리터럴만 잔존) |
| 14 | 도메인 타입 시그니처 | ✕ | ○ (`Object` view 1건 잔존) |
| 15 | 단위 테스트 가능성 | ✕ | ◎ |
| 16 | 도메인 어휘 반영 | △ | ◎ |

범례: ◎ 충실 / ○ 양호 / △ 부분적 / ✕ 미반영

---

## 결론

- **L1**은 Use Case Diagram의 시각적 요소(액터·UC·include·external system)를 1:1로 옮긴 **스켈레톤**. 코드 구조는 깔끔하나 모든 본문이 TODO이며 도메인·예외·정책이 부재해 그대로 테스트·실행 불가.
- **L2**는 Use Case Description의 Step / Alternatives / Pre·Postcondition / 부록 §1~§4 를 흡수하여, **단계별 메서드 호출 + 예외 분기 + 도메인 객체 + 정책 상수**를 모두 갖춘 구현 직전 단계의 코드.
- 정량적으로 클래스 약 3.4×, 정성적으로는 "도식 → 실행 가능 설계"로의 질적 전환이 완료됨. 단위 테스트, 분기 커버리지, 명세 추적성 모두 L2에서만 성립.
- L2 잔존 개선점: ① 락 해제 `try/finally` 보강, ② `EditItineraryResponse.updatedItineraryView` 구체 타입화, ③ 예외 메시지 문자열을 `ErrorCode.name()`으로 통합, ④ `AuthenticateUser` 등 행위 클래스의 인터페이스 추출.
