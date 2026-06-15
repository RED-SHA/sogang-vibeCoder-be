# Run2 발표자료용 핵심 코드 정리

## 1. 기본 정보

| 항목 | 내용 |
|---|---|
| 브랜치 | `experiment/L5-chatgpt-run2` |
| 구현 Commit | `884669d feat(l5): generate UC-ADM-07 implementation run2` |
| 증빙 Commit | `0bd6d66 feat(l5): generate UC-ADM-07 evidence run2` |
| 대상 Use Case | UC-ADM-07 Edit Itinerary |
| 기준 명세 | `Output_L5/docs/WS3_RED-SHA_K-MedTour_DynamicModeling.md` |
| 핵심 구현 파일 | `Output_L3+L4/src/main/java/com/kmedical/control/JourneyController.java` |
| 발표용 증빙 위치 | `Output_L5/evidence/run2/` |

## 2. 발표자료에 넣을 핵심 코드 캡처

| 주제 | 사용할 증빙 이미지 | 실제 코드 위치 | 발표에서 증명할 내용 |
|---|---|---|---|
| 상태 전이 | `04_session_transition.png` | `EditItineraryStatus.java:4-20`, `JourneyController.java:631-667` | UC-ADM-07 편집 세션 상태를 별도 enum으로 두고, 허용된 상태 전이만 통과시킨다. |
| edit lock | `05_edit_lock.png` | `JourneyController.java:173-181`, `368-374`, `276-280` | `putIfAbsent`로 schedule item 단위 편집 락을 획득하고, 이미 보유 중이면 `LOCK_HELD`로 즉시 거절하며, `finally`에서 해제한다. |
| 임계영역 | `06_critical_section.png` | `JourneyController.java:191-235` | 검증, version 비교, entity 변경, version 증가, 담당자 목록 반영이 같은 `synchronized` 블록 안에서 실행된다. |
| 감사 로그 위치 | `07_audit_outside_lock.png` | `JourneyController.java:237-240` | 변경 전후 diff 감사 로그가 임계영역 종료 후 실행된다. |
| concurrent 처리 | `08_concurrent_future.png` | `JourneyController.java:242-251` | 실시간 동기화와 push 알림이 `CompletableFuture.supplyAsync`로 독립 실행되고, `join()`으로 결과를 수집한다. |

## 3. 상태 전이 구현 요약

### 코드 위치

* 상태 enum: `Output_L3+L4/src/main/java/com/kmedical/domain/enums/EditItineraryStatus.java:4-20`
* 세션 객체: `Output_L3+L4/src/main/java/com/kmedical/control/JourneyController.java:631-667`
* 주요 전이 호출: `Output_L3+L4/src/main/java/com/kmedical/control/JourneyController.java:151-272`

### 발표 핵심 문장

Run2는 `ScheduleItemStatus`와 별도로 `EditItineraryStatus`를 만들어, 일정의 업무 진행 상태와 편집 use case 실행 상태를 분리했다.  
각 요청마다 `EditItinerarySession`을 새로 만들고, `transition(next, event)`를 통해서만 상태를 바꾸도록 했다.

### 상태 목록

`INIT -> AUTH_CHECKED -> ITEM_SELECTED -> LOCK_ACQUIRED -> EDIT_FORM_RETURNED -> SUBMITTED -> VALIDATED -> PERSISTED -> AUDIT_LOGGED -> SYNC_REQUESTED -> PUSH_REQUESTED -> READY`

대안 흐름:

* 권한 거절: `INIT -> REJECTED`
* 락 충돌: `ITEM_SELECTED -> REJECTED`
* 검증 실패: `SUBMITTED -> EDIT_FORM_RETURNED`
* 버전 충돌: `VALIDATED -> REJECTED`
* 사용자 취소: `EDIT_FORM_RETURNED -> CANCELLED`
* 실시간 동기화 저하: `SYNC_REQUESTED -> SYNC_DEGRADED -> PUSH_REQUESTED`

### 인용할 코드 포인트

```java
private static final class EditItinerarySession {
    private EditItineraryStatus status = EditItineraryStatus.INIT;

    void transition(EditItineraryStatus next, String event) {
        if (!isAllowed(status, next)) {
            throw new IllegalStateException(
                    "Invalid UC-ADM-07 transition: " + status + " --" + event + "--> " + next);
        }
        status = next;
    }
}
```

## 4. Edit Lock 구현 요약

### 코드 위치

* 락 획득 및 A7 처리: `JourneyController.java:173-181`
* 락 자료구조 접근: `JourneyController.java:368-374`
* 락 해제 보장: `JourneyController.java:276-280`

### 발표 핵심 문장

Run2는 `scheduleItemId`를 key로 하는 edit lock을 사용한다.  
`putIfAbsent`가 기존 보유자를 반환하면 대기하지 않고 즉시 `LOCK_HELD` 오류 응답으로 종료한다.

### 인용할 코드 포인트

```java
private String acquireEditLock(String scheduleItemId, String operatorId) {
    return editLocks.putIfAbsent(scheduleItemId, operatorId);
}

private void releaseEditLock(String scheduleItemId, String operatorId) {
    editLocks.remove(scheduleItemId, operatorId);
}
```

```java
String lockHolder = acquireEditLock(scheduleItemId, operatorId);
if (lockHolder != null) {
    session.transition(EditItineraryStatus.REJECTED, "lockHeld");
    return EditItineraryResponseDTO.error(
            EditItineraryErrorCode.LOCK_HELD,
            lockHolder,
            session.getStatus());
}
```

## 5. 임계영역 구현 요약

### 코드 위치

* 임계영역 시작: `JourneyController.java:190-191`
* 검증: `JourneyController.java:206-213`
* version 비교: `JourneyController.java:215-227`
* 변경 적용/version 증가/담당자 반영: `JourneyController.java:229-234`
* 임계영역 종료: `JourneyController.java:235`

### 발표 핵심 문장

Run2는 같은 schedule item에 대한 수정 요청이 검증과 저장 사이에 끼어들지 못하도록 `scheduleItemId`별 mutex를 사용한다.  
검증, 낙관적 version 비교, entity 변경, version 증가, 담당자 반영을 하나의 `synchronized` 블록 안에서 처리한다.

### 인용할 코드 포인트

```java
Object mutex = scheduleItemMutexes.computeIfAbsent(scheduleItemId, k -> new Object());
synchronized (mutex) {
    ScheduleItem item = findScheduleItem(scheduleItemId);

    Map<String, String> fieldErrors = validateEditPayload(request, item);
    if (!fieldErrors.isEmpty()) {
        session.transition(EditItineraryStatus.EDIT_FORM_RETURNED, "validationFailed");
        return EditItineraryResponseDTO.validationError(fieldErrors, session.getStatus());
    }

    int expectedVersion = request.getExpectedVersion();
    int actualVersion = item.getVersion();
    if (expectedVersion != actualVersion) {
        ...
    }

    applyScheduleChanges(item, request);
    item.setVersion(item.getVersion() + 1);
    assignedStaffIds = new ArrayList<>(item.getAssignedStaffIds());
    after = toItemDTO(item);
    session.transition(EditItineraryStatus.PERSISTED, "persistOk");
}
```

## 6. Concurrent 처리 구현 요약

### 코드 위치

* concurrent 실행: `JourneyController.java:242-248`
* 결과 수집: `JourneyController.java:250-251`
* degraded sync 처리: `JourneyController.java:253-263`
* push warning 처리: `JourneyController.java:265-272`

### 발표 핵심 문장

Run2는 저장이 끝난 뒤 실시간 동기화와 push 알림을 핵심 임계영역 밖에서 병렬로 실행한다.  
두 작업은 `CompletableFuture.supplyAsync`로 동시에 시작되고, `join()`으로 각각의 성공 여부를 수집한다.

### 인용할 코드 포인트

```java
CompletableFuture<Boolean> syncFuture =
        CompletableFuture.supplyAsync(() -> publishRealtimeWithRetry(after));
CompletableFuture<Boolean> pushFuture =
        CompletableFuture.supplyAsync(() -> queuePushWithRetry(assignedStaffIds, patientEndpoint, after));

boolean syncOk = syncFuture.join();
boolean pushOk = pushFuture.join();
```

### 발표 시 강조할 점

* `sync` 실패는 일정 저장을 rollback하지 않고 `degradedSync=true`로 응답한다.
* `push` 실패도 본문 변경을 rollback하지 않고 `pushWarning=true`로 응답한다.
* 외부 I/O를 lock 밖으로 밀어내 장기 lock 위험을 줄였다.

## 7. 슬라이드 구성 제안

| 슬라이드 | 제목 | 넣을 자료 | 핵심 메시지 |
|---|---|---|---|
| 1 | Run2 생성 결과 | `01_generation_result.png` | L5 명세 기반 구현과 빌드가 완료됐다. |
| 2 | 상태 전이 설계 | `04_session_transition.png` | 편집 use case 상태를 별도 session state로 분리했다. |
| 3 | 편집 충돌 처리 | `05_edit_lock.png` | edit lock 충돌은 대기 없이 `LOCK_HELD`로 즉시 거절한다. |
| 4 | 원자적 변경 구간 | `06_critical_section.png` | 검증부터 version 증가까지 같은 임계영역에서 수행된다. |
| 5 | 감사 로그와 외부 출력 분리 | `07_audit_outside_lock.png` | 감사 로그는 저장 후, 외부 출력 전, lock 밖에서 기록된다. |
| 6 | Concurrent 출력 | `08_concurrent_future.png` | sync와 push는 `CompletableFuture`로 병렬 실행된다. |
| 7 | 한계와 편차 | `02_review_result.png` | 준수/부분/미준수 항목을 실험 결과로 제시한다. |
| 8 | 실행 가능성 | `09_build_success.png` | 195개 클래스 컴파일, JAR 생성, 서버 기동이 확인됐다. |

## 8. 발표자료에 적을 한계

1. A6는 실제 RBAC 저장소 조회가 아니라 `operatorId` 존재 여부를 권한 gate로 사용한다.
2. A3의 지연 delta는 실제 outbox 저장소가 아니라 감사 warning 로그 중심으로 남는다.
3. `CompletableFuture.join()`은 boolean 결과는 수집하지만, 예외를 별도 에러 DTO로 세밀하게 변환하지는 않는다.
4. A1 검증 실패 시 lock 유지 정책은 명세상 확인 필요 영역이며, 실제 구현은 응답 반환 후 `finally`에서 lock을 해제한다.
5. patient magic link endpoint는 request DTO 필드 또는 fallback 문자열을 사용하는 수준이다.

## 9. 발표자료용 짧은 설명문

Run2는 UC-ADM-07의 L5 Dynamic Modeling 명세를 실제 L4 코드에 투영한 실험이다.  
핵심은 세 가지다.

첫째, `EditItineraryStatus`와 `EditItinerarySession.transition()`으로 편집 use case의 상태 전이를 명시적으로 만들었다.  
둘째, `scheduleItemId` 단위 edit lock과 `synchronized` 임계영역을 통해 검증, version 비교, 변경, version 증가가 원자적으로 실행되도록 했다.  
셋째, 저장 이후의 실시간 동기화와 push 알림은 `CompletableFuture`로 lock 밖에서 병렬 실행해, 외부 I/O 실패가 핵심 일정 변경 성공 여부를 막지 않도록 했다.

