이전 작업에서 생성된 L3/L4 기준의 Entity, Control, Interface(DTO 포함) 클래스들을 기반으로, 실제 시스템을 구동하기 위한 진입점인 `App.java`와 외부 HTTP 요청을 처리할 `HTTP Handler(Controller 역할)` 코드를 생성하라. 대규모 작업임을 고려하여, 토큰 제한으로 중단되더라도 다음 에이전트가 완벽히 이어받을 수 있도록 상태 추적 하네스(Harness) 규칙을 엄격히 준수하라.

[출력 경로]
* /Users/mac/sogang/sogang-vibeCoder-be/Output_L3+L4/src/main/java/...

0. 언어 및 환경 (Language & Environment)
* Pure Java (Java SE) 표준 라이브러리만 사용할 것. (Spring, Tomcat, Spring Boot 등 외부 웹 프레임워크 절대 사용 금지)
* HTTP 서버 구현은 반드시 Java 표준 라이브러리인 `com.sun.net.httpserver.HttpServer`를 사용한다.
* JSON 파싱 등의 작업이 필요한 경우, 별도 지시가 없다면 Pure Java 수준에서 처리할 수 있는 간단한 파서 유틸리티를 작성하거나 표준 라이브러리를 활용한다.

1. 구현 원칙 (절대 준수)
* 1.1 App.java (System Bootstrapper) 규칙
    * 시스템의 메인 진입점(`public static void main`) 역할을 수행한다.
    * 외부 DI 프레임워크 없이 **Pure Java를 이용한 수동 의존성 주입(Manual DI)**을 수행한다. (Entity 인스턴스화 -> Control 객체에 주입 -> Handler 객체에 Control 주입)
    * `HttpServer`를 초기화하고, 생성된 Handler들을 지정된 컨텍스트 경로(ex: `/api/...`)에 매핑한 뒤 서버를 시작(Start)한다.
    * 시스템 초기화 시 Control 객체의 상태를 알맞게 설정한다 (예: Operator UC의 StartUp 프로세스 호출).
* 1.2 HTTP Handler (Interface Object 매핑) 규칙
    * 기존 L3/L4 명세에 정의된 Interface Object의 역할을 받아, 실제 HTTP Request를 파싱하고 HTTP Response를 반환하는 웹 계층 역할을 수행한다.
    * HTTP 요청 데이터를 추출하여 앞서 정의된 **DTO로 변환**한 후 Control 객체로 전달한다.
    * 비즈니스 로직을 절대 포함하지 않으며, 오직 "요청 파싱 -> Control 위임 -> 응답 포맷팅" 역할만 수행한다.
* 1.3 Exception & HTTP Status Mapping
    * Control 계층에서 발생하는 예외를 적절한 HTTP 상태 코드로 변환하여 응답한다 (예: 잘못된 입력 400 Bad Request, 서버 에러 500 Internal Server Error).
    * 특히 **ClosedDown 상태에서 발생하는 가드(Guard) 차단 예외는 503 Service Unavailable** 등으로 명확히 매핑하여 처리한다.
* 1.4 No Extension Rule
    * 기존에 구현된 Control이나 Entity 내부의 비즈니스 로직을 임의로 수정하거나 새로운 Use Case를 추가하지 않는다. 오직 연결(Wiring)과 노출(Exposing)에 집중한다.

2. 연속 작업 및 상태 추적 하네스 (Resumability & State Harness)
   코드 생성 시작 전 및 진행 과정에서 다음 파일들을 지정된 출력 경로 최상단에 반드시 생성/업데이트하라.
* 2.1 README.md (프로젝트 컨텍스트 업데이트)
    * HttpServer 기반의 실행 방법, 매핑된 API 엔드포인트 목록, 수동 DI 구조에 대한 설명을 추가한다.
* 2.2 PROGRESS_TRACKER.md (진척도 관리)
    * `App.java` 및 매핑해야 할 모든 `HttpHandler` 목록을 Markdown 체크리스트(- [ ] 클래스명) 형태로 추가한다.
    * 코드가 생성될 때마다 체크리스트를 완료 상태(- [x] 클래스명)로 실시간 업데이트한다.
* 2.3 NEXT_AGENT_PROMPT.md (자동 인수인계)
    * 현재 에이전트의 출력이 끝나기 직전(또는 토큰 한계 도달 예상 시), 다음 에이전트가 그대로 복사하여 사용할 수 있는 프롬프트 파일을 생성한다.
    * 이 파일에는 1) 생성 완료된 Handler 목록, 2) 이어서 생성해야 할 Handler 또는 마무리 작업 목록, 3) Pure Java 및 Manual DI 원칙 요약이 포함되어야 한다.

3. 출력 규칙
* 전체 소스코드를 명확한 파일 단위(파일명 및 경로 명시)로 분리하여 출력한다.
* 패키지 구조 트리(src/main/java/...)를 상단에 포함한다.
* 누락된 클래스나 메서드 없이 100% 완전한 코드로 생성한다(구현부 생략 금지).
* 코드 이외의 불필요한 부연 설명은 최소화한다.

4. 검증 단계 (필수)
   에이전트는 1회차 출력을 마치기 전, 스스로 다음 항목에 대한 Self-Check 결과를 요약하여 출력하라:
* PROGRESS_TRACKER.md 업데이트 완료 여부
* NEXT_AGENT_PROMPT.md 생성 완료 여부
* App.java 내 Manual DI 및 HttpServer 정상 셋업 여부
* Handler 계층에서 Control로 데이터 전달 시 DTO 사용 여부
* ClosedDown 가드 예외가 HTTP 503 (또는 적절한 상태코드)으로 처리되었는지 여부