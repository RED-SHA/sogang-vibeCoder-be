# Use Case Description — Edit Itinerary (UC-ADM-07)

> 입력: `docs/requirements.md`, `docs/UseCaseModeling.md` §2-2 / §3 / §4
> 대상 Req: ADM-402 / NFR 실시간 동기화 / NFR 데이터 정합성
> 생성: 2026-04-25

---

```
Use Case Name : Edit Itinerary

Summary : 에이전시 운영자는 환자 여정 일정을 수정하여 배정된 모든 관계자에게 갱신된 스케줄이 보이도록 한다.

Actor : 에이전시 운영자 (Agency Operator)

Dependency :
  - Synchronize Realtime State
  - Send Push Notification
  - Audit Log Action

Precondition :
  1. 에이전시 운영자가 유효한 JWT와 Agency Admin 권한으로 인증되어 있다.
  2. 대상 환자 케이스가 ACTIVE 상태이며 영속화된 일정 항목이 1개 이상 존재한다.
  3. 대상 일정 항목이 다른 운영자에 의해 편집 락에 걸려 있지 않다.
  4. 해당 케이스의 Realtime Sync Bus 채널이 열려 있다.
  5. Push Notification Gateway가 애플리케이션 서비스에서 도달 가능하다.
  6. 감사 로그 기록기가 audit_log 테이블에 쓰기 가능 상태이다.

Description :
  1. 에이전시 운영자는 케이스 대시보드에서 대상 일정 항목을 선택한다.
  2. 시스템은 해당 일정 항목에 편집 락을 획득하고 현재 스케줄을 편집 가능한 폼과 함께 반환한다.
  3. 에이전시 운영자는 시작 시각, 종료 시각, 장소, 배정 실무자 식별자를 포함한 수정 필드를 제출한다.
  4. 시스템은 페이로드 스키마, 비즈니스 규칙, 형제 항목과의 시간대 중복을 검증한다.
  5. 시스템은 journey_schedule 테이블에 새 리비전을 기록하고 version을 증가시키며 assigned_staff 연관을 갱신한다.
  6. 시스템은 운영자 식별자, 변경 전후 diff, ISO 8601 타임스탬프와 함께 Audit Log Action 을 include 한다.
  7. 시스템은 구독 중인 환자·실무자 클라이언트에 갱신된 스케줄 스냅샷을 5초 이내 전송하기 위해 Synchronize Realtime State 를 include 한다.
  8. 시스템은 배정된 모든 실무자와 환자 매직 링크 엔드포인트로 변경 알림을 큐잉하기 위해 Send Push Notification 을 include 한다.
  9. 시스템은 일정 항목의 편집 락을 해제한다.
 10. 시스템은 갱신된 일정 뷰를 에이전시 운영자에게 반환한다.
 11. 시스템은 대시보드 준비 상태로 복귀한다.

Alternatives :
  A1. 4 단계에서 스키마 오류·과거 시각·형제 항목 중복으로 검증이 실패하면, 시스템은 필드 수준 오류 마커와 함께 제출을 거부한다. 3 단계로 복귀한다.
  A2. 5 단계에서 다른 운영자의 동시 편집으로 낙관적 락 버전 충돌이 감지되면, 시스템은 쓰기를 중단하고 최신 리비전을 다시 로드해 머지 뷰를 표시한다. 2 단계로 복귀한다.
  A3. 7 단계에서 Realtime Sync Bus 발행 호출이 실패하면, 시스템은 지수 백오프로 최대 3회 재시도한다.
       A3.1. 최종 실패 시 시스템은 outbox 테이블에 delta 를 기록해 지연 발송을 예약하고 응답에 degraded_sync 플래그를 설정한다.
       A3.2. degraded_sync 플래그가 설정된 채로 유스케이스를 종료한다.
  A4. 8 단계에서 Push Notification Gateway 가 5xx 응답을 반환하면, 시스템은 재시도 정책에 따라 알림을 재시도 큐에 등록한다.
       A4.1. 재시도가 모두 소진되면 시스템은 실패를 알림 센터에 기록하고 진행을 계속한다. 9 단계로 복귀한다.
  A5. 2 단계와 5 단계 사이에서 에이전시 운영자가 편집을 취소하면, 시스템은 임시 페이로드를 폐기하고 편집 락을 해제하며 유스케이스를 종료한다.
  A6. 1 단계에서 RBAC 검사가 대상 케이스에 대한 쓰기 권한을 거부하면, 시스템은 행위를 차단하고 운영자 식별자·거부 사유와 함께 Audit Log Action 을 include 하며 권한 오류 코드를 반환한다. 유스케이스를 종료한다.
  A7. 2 단계에서 다른 편집자가 락을 보유 중이라 락 획득에 실패하면, 시스템은 락 보유자 식별자를 포함한 lock_held 오류를 반환한다. 유스케이스를 종료한다.

Postcondition :
  1. journey_schedule 레코드가 갱신되고 version 필드가 단조 증가한다.
  2. assigned_staff 연관이 제출된 실무자 식별자를 반영하며 고아 행이 존재하지 않는다.
  3. audit_log 항목이 운영자 식별자·변경 전후 diff·ISO 8601 타임스탬프와 함께 영속화된다.
  4. 구독 중인 환자·실무자 클라이언트에 5초 이내 실시간 스냅샷이 전달되거나, degraded_sync 플래그가 설정되며 outbox 항목이 큐잉된다.
  5. 배정된 모든 실무자 식별자와 환자 매직 링크 엔드포인트에 대해 푸시 알림 작업이 큐잉된다.
  6. 일정 항목의 편집 락이 해제된다.
  7. 실패 종료 시 (A1, A2, A6, A7): journey_schedule 레코드는 변경되지 않고, 본문 변경에 대한 audit diff 가 기록되지 않으며, 알림이 발송되지 않는다.
```

---

## 부록: 단계 ↔ 의존 Abstract UC 트레이스

| Description 단계 | Abstract UC | 트리거 의미 |
|-----------------|-------------|------------|
| 6 | Audit Log Action | 변경 이력 영속화 (NFR 데이터 정합성, RBAC 추적) |
| 7 | Synchronize Realtime State | 5초 이내 실시간 반영 (NFR 실시간 동기화) |
| 8 | Send Push Notification | 다중 수신자 알림 (Req ADM-402) |

> Authenticate User 는 Precondition (활성 JWT) 시점에 이미 종료된 상태로 가정 → Description include 대상 아님.
