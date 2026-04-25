# WS1 Use Case Description Rules — K-Medical Concierge OS

> Step 2-1 Use Case Description 생성 프롬프트 [규칙] 영역.
> Subject 기본 양식 + `requirements.md` + `docs/api-spec.md` + `docs/sprints/*` 사전 분석 결과 반영.
> ISO 29148:2018 품질 특성(Necessary, Appropriate, Unambiguous, Complete, Singular, Feasible, Verifiable, Correct, Conforming) 준수를 전제로 한다.

---

## [규칙]

1. **필드 구성** — 아래 순서와 형식을 반드시 준수한다
   ```
   Use Case Name :
   Summary :
   Actor :
   Dependency : (Include 관계가 없으면 생략)
   Precondition :
   Description :
   Alternatives :
   Postcondition :
   ```
   - 필드명·콜론·공백·줄바꿈을 위 양식과 동일하게 유지한다
   - Dependency 필드가 비어 있으면 라인 자체를 삭제한다 (빈 값으로 두지 않는다)
   - 모든 필드는 영문 명사구·평서문으로 작성하고, 마침표는 단계 종결에만 사용한다
   - 단계 번호는 `1.`, `2.`, `2.1.` 형태의 점 표기 + 들여쓰기로 계층을 구분한다

2. **Use Case Name**
   - 동사+명사 영문 형태로 작성한다 (예: `Issue Magic Link`, `Update Itinerary Schedule`, `Dispatch Operational Staff`)
   - `WS1_UseCaseModelingRules.md`에서 부여한 UC ID와 1:1로 매칭되는 명칭을 사용하고 동의어로 변경하지 않는다
   - 시스템 내부 절차(예: `Encrypt Payload`)가 아니라 Actor의 비즈니스 목표를 표현한다

3. **Summary**
   - 주어+동사+목적어 형태의 단일 문장으로 작성한다
   - 주어는 Use Case의 Primary Actor, 목적어는 시스템이 제공하는 산출물 또는 상태 변화로 한정한다 (예: `The Agency Operator dispatches a translator and a driver to a patient itinerary.`)
   - "in order to ~", "so that ~" 등 부가절을 1회까지만 허용하여 의도를 명확히 한다
   - 비기능 요구사항(다국어, 5초 이내 동기화, GDPR 등)은 Summary에 직접 노출하지 않고 Description/Alternatives에서 다룬다

4. **Actor**
   - 이 Use Case를 직접 시작하는 외부 행위자(Primary Actor)만 명시한다
   - Secondary Actor(External System, 푸시 게이트웨이, 번역 엔진 등)는 Description의 호출 단계에서 언급하고 Actor 필드에 적지 않는다
   - 동일 직무라도 인증 경로가 다르면 분리해 표기한다 (예: `Registered Patient` vs `Magic Link Guest Patient`)
   - Operator/시스템 자동 트리거의 경우 `System Scheduler`처럼 명시적 Actor 명을 사용한다

5. **Dependency**
   - `«include»` 관계로 호출되는 Abstract UC만 나열한다 (`«extend»`, Generalization은 제외)
   - 호출 순서대로 위에서 아래로 적고, 각 항목은 `- Authenticate User` 형태의 영문 UC Name을 사용한다
   - 한 UC가 여러 Abstract UC를 호출하면 모두 나열한다 (예: 일정 변경 UC → `Authenticate User`, `Synchronize Realtime State`, `Send Multi-Channel Notification`, `Append Audit Log`)
   - Description 본문에서 호출하지 않는 Abstract UC는 Dependency에 적지 않는다 (불일치 금지)

6. **Precondition**
   - "None" 또는 모호한 표현을 금지하고, 검증 가능한 시스템·데이터 상태로 서술한다
   - 인증·세션 상태(예: `The Agency Operator is authenticated with an active JWT and Admin role.`)와 도메인 상태(예: `The patient case is in CONTRACTED status.`)를 분리해 작성한다
   - NFR이 요구하는 사전 상태(매직 링크 2차 인증 통과, GDPR 동의 완료, 다국어 헤더 인식 등)를 항목별로 분리한다
   - 외부 시스템 가용성(번역 엔진, WebSocket 채널, S3 등)은 Description이 의존하는 경우에만 추가한다

7. **Description (정상 흐름)**
   - 첫 단계에 `«include»` Abstract UC 호출을 명시한다 (예: `1. The system includes Authenticate User.`)
   - 한 단계에는 한 행동만 기술한다 (Singular). "and"로 두 행동을 묶지 않는다
   - 행위 주체를 매 단계 명시한다 (`The Agency Operator ...`, `The system ...`, `The Notification Gateway ...`)
   - 시스템 응답은 직후 단계로 분리해 입력→처리→응답 패턴을 유지한다
   - 외부 호출(번역 엔진, 푸시 게이트웨이, EMR 미연동이므로 EMR 제외)은 Secondary Actor 명을 명시한다
   - 실시간 동기화 단계는 "5초 이내 반영"과 같은 검증 가능한 표현으로 작성한다 (NFR 추적성)
   - 마지막 단계는 정상 상태 복귀(`The system returns to the dashboard ready state.` 등)로 마무리한다

8. **Alternatives (예외 및 대안 흐름)**
   - 각 항목은 `A1.`, `A2.` 형태로 번호를 부여하고 분기 시점을 본문 단계 번호로 명시한다 (예: `A1. At step 3, if the staff is unavailable, ...`)
   - Cancel 흐름은 별도 Alternative로 정의하고 자원 회수(예약 락 해제, 임시 파일 삭제)와 정상 종료를 분리해 단계를 적는다
   - 외부 시스템 실패(번역 엔진 timeout, 푸시 게이트웨이 5xx)는 재시도 정책 또는 Fallback 단계로 명시한다
   - 보안 위반(매직 링크 만료, 2차 인증 실패, RBAC 위반)은 사용자 메시지 + 감사 로그 기록 단계를 포함한다
   - 각 Alternative는 종료 시점에 본문 단계로 복귀하는지(`Resume at step 5.`) 또는 UC를 종료하는지(`The use case ends.`)를 명시한다

9. **Postcondition**
   - 시스템·데이터 상태로 서술한다 (UI 메시지·토스트는 금지)
   - 정상 흐름 완료 시 변경된 엔티티 상태(예: `The journey schedule is updated and version is incremented.`)와 외부 효과(`A push notification is queued for the assigned staff.`)를 모두 적는다
   - 데이터 정합성 NFR을 반영해 "DB 타임라인이 오차 없이 갱신됨" 등 검증 가능한 조건을 포함한다
   - 실패 종료(Alternative)에 대한 Postcondition이 별도로 필요한 경우 `On failure: ...`로 분리해 작성한다

---

## [필드 간 정합성 체크]

- Dependency에 명시된 Abstract UC는 Description 본문에서 모두 호출되어야 한다
- Precondition에서 보장한 상태는 Description에서 다시 검증하지 않는다 (중복 금지)
- Alternative의 분기 조건은 Precondition에 흡수되지 않은, 런타임에만 판별 가능한 조건만 다룬다
- Postcondition은 Description의 마지막 단계 또는 Alternative 종결 단계와 1:1로 대응되어야 한다
- 비기능 요구사항(다국어/실시간/보안/정합성/프라이버시)은 Description 또는 Alternative 단계로 추적 가능해야 한다 (Summary에만 적고 본문에서 누락 금지)
