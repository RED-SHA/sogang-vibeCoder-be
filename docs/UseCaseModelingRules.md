# WS1 Use Case Modeling Rules — K-Medical Concierge OS

> Step 1-1 Use Case Diagram 생성 프롬프트 [규칙] 영역.
> Subject 기본 양식 + `requirements.md` + `docs/` 사전 분석 결과 반영.

---

## [규칙]

1. **Actor 식별**
   - Problem Description에 등장하는 모든 외부 행위자를 빠짐없이 식별한다
   - Human Actor와 External System Actor를 분리하고 External은 `<<external>>` 스테레오타입으로 표기한다
   - 동일 직무라도 인증 경로·권한이 다르면 별도 Actor로 분리한다 (예: 정식 가입 환자 vs 매직 링크 비회원 환자)
   - 시스템 lifecycle을 책임지는 Operator Actor를 명시한다 (Start Up / Shut Down / 권한 정책 담당)
   - 3-side B2B2C 구조(에이전시 / 환자 / 실무자)를 그대로 유지하며 임의 통합하지 않는다
   - NFR이 요구하는 외부 의존(번역 엔진, 푸시 게이트웨이, 실시간 동기화 버스, 암호화 저장소 등)은 별도 External Actor로 도출한다

2. **Use Case 식별**
   - 동사+명사 영문 형태로 작성한다 (예: `Borrow Book`, `Validate Member`, `Edit Schedule`)
   - 1 Use Case = 1 Actor Goal 단위로 정의하고, Feature 단순 나열을 금지한다
   - 동일 Actor의 동일 목표를 갖는 다수 요구사항은 하나의 UC로 묶는다 (예: PAT-601 라이브 타임라인 + PAT-602 실시간 상태 동기화 → `View Live Itinerary` 1개)
   - 비기능 요구사항(NFR)은 단독 UC로 만들지 않고 Abstract UC 또는 include로 흡수한다
   - Trade-off로 명시 제외된 영역(EMR 연동, 글로벌 결제 등)에 대한 UC는 생성하지 않는다
   - Optional/P2 요구사항도 식별 대상에 포함하되, MVP 미구현 항목은 Diagram 또는 매핑표에 표기한다
   - UC ID를 부여하여 요구사항(Req ID)과 다대일 추적성을 유지한다

3. **Abstract UC 분리**
   - 복수의 Concrete UC에서 동일한 흐름이 반복되는 경우 Abstract Use Case로 분리한다
   - Abstract UC는 `«abstract»` 스테레오타입으로 표기한다
   - Abstract UC는 Actor가 직접 호출하지 않으며 반드시 Concrete UC를 통해서만 호출된다
   - 인증, 다국어 번역, 실시간 동기화, 푸시 알림, 2차 인증, 감사 로깅 등 cross-cutting 흐름은 Abstract UC 후보로 우선 검토한다
   - 단일 Concrete UC만이 호출하는 흐름은 Abstract로 분리하지 않고 해당 UC 내부 단계로 흡수한다

4. **UC 관계 표현**
   - `«include»` 화살표는 항상 실행되는 공통 흐름을 표현하며, 방향은 Concrete → Abstract (점선 화살표 시작점 = Concrete)
   - `«extend»` 화살표는 조건부로 발생하는 흐름(선택, 예외, 옵션)을 표현하며, 방향은 Extension → Base (점선 화살표 시작점 = Extension)
   - Actor Generalization은 본질적 차이(인증 경로 등)가 있을 때 사용하지 않고 별도 Actor로 분리한다
   - External System은 우측에 secondary actor로 배치하고 UC → External 의존 화살표로 외부 호출을 표현한다
   - 하나의 Concrete UC가 여러 Abstract UC를 동시에 include할 수 있다 (예: 일정 변경 UC ⊃ {실시간 동기화, 푸시 알림, 감사 로깅})
   - 상태머신을 갖는 도메인의 상태 전이는 별도 UC로 쪼개지 않고 단일 UC의 Description 또는 Alternatives로 표현한다

5. **기타**
   - System Boundary를 명시한다 (`rectangle "<시스템명>" { ... }`)
   - 모든 Actor는 최소 1개 이상의 UC와 연결한다
   - 고립된 UC(어떤 Actor와도 연결되지 않고 include/extend로도 결합되지 않은 UC)가 없도록 한다
   - Surface(영역)별로 package를 사용하여 그룹핑한다 (예: System Operation / Agency Back-Office / Patient Concierge / Staff Mobile)
   - Human Actor는 좌측, External System Actor는 우측에 배치하여 좌→우 read flow를 유지한다
   - UC 이름 옆 또는 주석으로 UC ID를 표기하여 요구사항·API와의 추적성을 확보한다
   - 다국어/암호화/실시간 동기화 같은 NFR은 별도 UC로 노출하지 않고 Abstract UC 또는 의존 외부 시스템으로만 표현한다
   - Cross-cutting 모듈(예: 파일 업로드)은 단독 UC로 만들지 않고 호출 측 UC의 외부 의존으로 표현한다
