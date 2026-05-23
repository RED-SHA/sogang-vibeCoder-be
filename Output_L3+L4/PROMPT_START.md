다음 L3 Static Modeling 및 L4 Object Structuring 명세를 기반으로 전체 시스템 소스 코드를 생성하라. 대규모 작업임을 고려하여, 토큰 제한으로 중단되더라도 다음 에이전트가 완벽히 이어받을 수 있도록 상태 추적 하네스(Harness) 규칙을 엄격히 준수하라.

[입력 명세]
* /Users/mac/sogang/K의료관광솔루션/docs/L3_Static_Modeling_K의료관광솔루션.md
* /Users/mac/sogang/K의료관광솔루션/docs/L4_Object_Structuring_K의료관광솔루션.md

[출력 경로]
* /Users/mac/sogang/sogang-vibeCoder-be/Output_L3+L4

0. 언어 및 환경 (Language & Environment)
* Pure Java (Java SE) 표준 라이브러리만 사용할 것.
* Spring 등 외부 프레임워크나 서드파티 라이브러리(Lombok 등 제외 시 명시) 사용을 엄격히 금지한다.

1. 구현 원칙 (절대 준수)
* 1.1 L3/L4 Strict Mapping
    * L3 Class는 반드시 1:1로 코드로 구현한다.
    * L4 Object Structuring에 정의된 객체를 그대로 구현한다.
    * 클래스 이름, 책임, 구조를 임의 변경하는 것을 금지한다.
    * 명세에 없는 기능의 추가 및 삭제를 금지한다.
* 1.2 Architecture Separation (C/S + Entity/Control)
    * Interface Object 규칙:
        * Interface Object는 외부 입력을 받아 Control Object로 전달하고 결과를 출력하는 역할만 수행한다. 비즈니스 로직을 절대 포함하지 않는다.
        * «user interface» 객체의 메서드는 Use Case Description의 Actor 행동 단계에서만 도출한다.
        * «input/output device interface» 및 «output device interface» 객체의 메서드는 장치 I/O 단계(카드 삽입, 현금 배출 등) 기반으로만 구성한다.
        * Operator Interface Object는 Customer Interface Object와 반드시 분리하여 별도 클래스로 설계한다.
    * Control Object 규칙:
        * «state dependent control» 객체의 상태는 enum으로 정의하고, 현재 상태를 인스턴스 변수로 반드시 유지한다.
        * Control Object의 메서드는 Use Case Description의 System Response 단계 기반으로만 구현한다.
        * Operator UC (StartUp / CloseDown)를 처리하는 메서드는 Control Object에 별도로 구현한다.
        * ClosedDown 상태에서는 모든 Customer UC 진입을 차단하는 가드(Guard) 조건을 메서드 진입부에 필수로 구현한다.
    * Entity 규칙:
        * Entity는 상태 및 데이터만 보유한다. 비즈니스 로직 및 외부 I/O 포함을 금지한다.
* 1.3 Data Transfer Rule
    * Interface ↔ Control 간 데이터 전달은 DTO(Data Transfer Object)로만 수행한다.
    * Domain Entity를 Interface 계층에 직접 노출하는 것을 금지하며, 필요시 복사 객체(DTO)를 사용한다.
* 1.4 Dependency Rule
    * Interface → Control 단방향 호출만 허용한다.
    * Control → Entity 단방향 접근만 허용한다. 역방향 호출을 엄격히 금지한다.
* 1.5 Exception & Guard Rule
    * 모든 Control 메서드는 예외 처리 기준을 포함해야 한다.
    * ClosedDown 상태 접근 시(가드 조건 실패 시) 표준 예외를 반환하도록 처리한다.
* 1.6 No Extension Rule
    * L3/L4에 정의되지 않은 외부 기능 추가, 새로운 Use Case 생성, 명세에 없는 Helper/Utility 클래스의 과도한 임의 생성을 금지한다.

2. 연속 작업 및 상태 추적 하네스 (Resumability & State Harness)
   코드 생성 시작 전 및 진행 과정에서 다음 파일들을 지정된 출력 경로 최상단에 반드시 생성하고 유지하라.
* 2.1 README.md (프로젝트 컨텍스트)
    * 전체 아키텍처 원칙(Pure Java, 계층 분리 원칙 등)과 디렉토리 구조를 명시한다.
* 2.2 PROGRESS_TRACKER.md (진척도 관리)
    * 작업 시작 시 L3/L4 명세를 분석하여 생성해야 할 전체 클래스 및 DTO 목록을 Markdown 체크리스트(- [ ] 클래스명) 형태로 작성한다.
    * 코드가 생성될 때마다 체크리스트를 완료 상태(- [x] 클래스명)로 실시간 업데이트한다.
* 2.3 NEXT_AGENT_PROMPT.md (자동 인수인계)
    * 현재 에이전트의 출력이 끝나기 직전(또는 토큰 한계 도달 예상 시), 다음 에이전트가 그대로 복사하여 사용할 수 있는 프롬프트 파일을 생성한다.
    * 이 파일에는 1) 현재까지 완료된 작업 요약, 2) 다음에 이어서 생성해야 할 정확한 타겟 클래스 목록, 3) 0번과 1번의 핵심 원칙 요약이 포함되어야 한다.

3. 출력 규칙
* 전체 소스코드를 명확한 파일 단위(파일명 및 경로 명시)로 분리하여 출력한다.
* 패키지 구조 트리(src/main/java/...)를 상단에 포함한다.
* 누락된 클래스나 메서드 없이 100% 완전한 코드로 생성한다(구현부 생략 금지).
* 코드 이외의 불필요한 부연 설명은 최소화한다.

4. 검증 단계 (필수)
   에이전트는 1회차 출력을 마치기 전, 스스로 다음 항목에 대한 Self-Check 결과를 요약하여 출력하라:
* PROGRESS_TRACKER.md 업데이트 완료 여부
* NEXT_AGENT_PROMPT.md 생성 완료 여부
* L3 Class 1:1 구현 / L4 Object / 계층 의존성 규칙 준수 여부
* State Guard(ClosedDown 시 차단) 적용 여부 확인