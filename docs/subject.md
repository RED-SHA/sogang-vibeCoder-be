# COMET UML AI-Driven 소프트웨어 개발 - 과제

## 개요

각 팀의 **Problem Description**을 기준으로 아래 3단계 실험을 순서대로 수행합니다.

> 실험의 목적은 완성된 코드 작성이 아니라, **명세 수준에 따른 AI 코드 생성 품질의 차이를 관찰**하는 것입니다.

---

## Step 1. Use Case Modeling — UML 생성 지시 및 검토

### 목적

Problem Description 기준으로 Use Case Modeling 수행 → Diagram 생성 → 생성 결과를 아래 프롬프트에 포함된 규칙 기준으로 검토.

### 1-1. Use Case Diagram 생성

Problem Description을 분석하여 **Use Case Diagram을 PlantUML 코드로 생성**합니다.

> 아래 Problem Description을 분석하여 Use Case Diagram을 PlantUML 코드로 생성하세요.

#### [규칙]

> 예시로 작성된 내용입니다. 각 팀에서 Use Case 모델링을 위한 규칙 등을 정의하세요.

1. **Actor 식별**
   - Problem Description에 등장하는 모든 외부 행위자를 빠짐없이 식별한다
   -

2. **Use Case 식별**
   - 동사+명사 형태로 작성한다 (예: `Borrow Book`, `Validate Member`)
   -

3. **Abstract UC 분리**
   - 복수의 Concrete UC에서 동일한 흐름이 반복되는 경우 Abstract Use Case로 분리한다
   -

4. **UC 관계 표현**
   -
   -

5. **기타**
   - System Boundary를 명시한다
   - 모든 Actor는 최소 1개 이상의 UC와 연결한다
   - 고립된 UC(어떤 Actor와도 연결되지 않은 UC)가 없도록 한다

### 1-2. 생성 결과 검토

1-1의 수행 결과를 아래 [검토항목] 기준으로 검토, 문제점 발견 시 수정·보완하여 최종 결과물 생성.

#### [검토 항목]

1. Actor 수가 Problem Description의 행위자 목록과 일치하는가?
2. 모든 Actor가 Human / External System으로 구분되어 있는가?
3. Start Up / Shut Down Use Case가 Operator Actor와 연결되어 있는가?
4. 반복 공통 흐름이 Abstract UC로 분리되어 `«abstract»` 표시가 있는가?
5. `«include»` 화살표 방향이 Concrete → Abstract로 올바른가?
6. `«extend»` 화살표 방향이 Extension → Base로 올바른가?
7. 모든 Actor가 최소 1개 UC와 연결되어 있는가?
8. 고립된 UC가 없는가?
9. UC 이름이 동사+명사 형태인가?
10. System Boundary가 명시되어 있는가?

#### [출력 형식]

- 발견된 문제점 목록
- 수정된 PlantUML 코드

---

## Step 2. Use Case Description 수행 — 품질 검토 수행

### 목적

Step 1에서 식별된 UC 중 **핵심 Concrete UC**를 대상으로:

1. AI 생성 Use Case Description 수행
2. 학생 작성 Use Case Description 작성 (팀이 원하는 시스템 목표로 재정의)
3. 작성된 Use Case Description을 **ISO 29148:2018** 품질 기준으로 검토

### 2-1. Use Case Description 생성

아래 프롬프트를 활용하여 AI에게 Use Case Description 작성 요청:

> 아래 Problem Description을 바탕으로 지정된 Use Case의 Use Case Description을 작성하세요.

#### [대상 Use Case]

> 예: `Borrow Book`

#### [규칙]

> Use Case Description 작성에 필요한 규칙들을 작성하여 Description 생성

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

2. **Use Case Name**
   - 동사+명사 형태로 작성한다
   -

3. **Summary**
   - 주어+동사+목적어 형태의 단일 문장으로 작성한다
   -

4. **Actor**
   - 이 Use Case를 직접 시작하는 외부 행위자만 명시한다

5. **Dependency**
   -
   -

6. **Precondition**
   -
   -

7. **Description (정상 흐름)**
   -
   -

8. **Alternatives (예외 및 대안 흐름)**
   -
   -

9. **Postcondition**
   -

### 2-2. ISO 29148 품질 검토 지시

2-1의 결과를 아래 프롬프트를 활용하여 품질 검토.

#### [ISO 29148:2018 개별 요구사항 품질 특성]

| # | 특성 | 기준 |
|---|------|------|
| 1 | **Necessary** | 이 단계/내용이 없으면 Use Case가 불완전해지는가? |
| 2 | **Appropriate** | 구현 방법(How)이 아닌 기능(What)으로 서술되어 있는가? |
| 3 | **Unambiguous** | 읽는 사람마다 동일하게 해석할 수 있는가? |
| 4 | **Complete** | 이해에 필요한 모든 정보가 포함되어 있는가? |
| 5 | **Singular** | 한 단계에 하나의 행동 또는 반응만 기술되어 있는가? |
| 6 | **Feasible** | 실제로 구현 가능한 행동을 서술하고 있는가? |
| 7 | **Verifiable** | 테스트 또는 측정으로 충족 여부를 확인할 수 있는가? |
| 8 | **Correct** | 이해관계자의 실제 필요를 정확히 담고 있는가? |
| 9 | **Conforming** | 승인된 양식 및 작성 규칙을 준수하고 있는가? |

#### [추가 검토 항목]

- Precondition이 "None" 또는 모호한 표현이 아닌 **구체적 시스템 상태**로 서술되었는가?
- Description 첫 단계에 **Include Abstract UC**가 명시되었는가?
- **Cancel**이 별도 Alternative로 정의되었는가?
- **자원 회수**와 **정상 종료**가 명확히 구분되어 있는가?
- Postcondition이 **시스템 또는 데이터 상태**로 서술되었는가?
- 마지막 Description 단계가 **정상 상태 복귀**로 마무리되는가?

#### [출력 형식]

각 품질 특성별로 아래 형식으로 출력:

- **특성명** : Pass / Fail
- **대상 필드** : (해당 필드명)
- **근거** : (Pass 또는 Fail 판정 이유를 구체적으로 서술)
- **수정 제안** : (Fail인 경우에만 수정 방향 제시)

---

## Step 3. L1 코드 vs L2 코드 비교

### 목적

Step 1(UC Diagram 수준)과 Step 2(UC Description 수준)의 명세를 각각 AI에게 제공하여 코드를 생성하고, **두 결과의 구조적 차이**를 분석.

### 3-1. L1 수준 코드 생성 (UC Diagram 기반)

Step 1 최종 결과를 기준으로 코드 생성.

> 아래 Use Case Diagram 정보만을 바탕으로 순수 Java 코드를 생성하세요. *(각 팀의 구현언어로 재정의)*

#### [제약 조건]

- 외부 프레임워크(Spring, JPA 등)를 사용하지 않는다
- Use Case Diagram에 명시된 정보 이외의 내용은 임의로 추가하지 않는다
- 내부 구현 로직을 추론하여 채우지 않는다
- 구현할 수 없는 부분은 `// TODO` 주석으로 표시한다

### 3-2. L2 수준 코드 생성 (UC Description 기반)

Step 2 최종 Use Case Description 기준으로 코드 생성.

> 아래 Use Case Description을 바탕으로 순수 Java 코드를 생성하세요. *(각 팀의 구현언어로 재정의)*

#### [제약 조건]

- 외부 프레임워크(Spring, JPA 등)를 사용하지 않는다
- Use Case Description에 명시된 내용만을 근거로 구현한다
- 명시되지 않은 내부 구현 로직은 임의로 추가하지 않는다
- 구현할 수 없는 부분은 `// TODO` 주석으로 표시한다

#### [구현 지침]

> 아래는 예시이므로 각 팀에서 구현 지침을 작성하세요.

- Description의 각 단계를 메서드 호출 순서로 변환한다
- Include된 Abstract UC는 별도 독립 클래스로 구현한다
- 각 코드 라인 옆에 대응되는 Description 단계 번호를 주석으로 표시한다

### 3-3. 두 코드의 구조적 차이 분석

Step 1과 Step 2의 생성 코드를 비교.

#### [분석 항목]

> 아래 분석 항목 외에 팀이 자체적으로 판단하여 추가하세요.

1. 클래스 수 비교
2. Actor 구분 반영 여부
3. Abstract UC가 독립 클래스로 분리되었는가
4. `«include»` 관계가 코드에서 필드 또는 의존으로 표현되었는가
5. Description의 각 단계가 메서드로 구현되었는가
6. Alternatives의 예외 상황이 Exception 클래스로 구현되었는가
7. Cancel이 별도로 처리되었는가
8. Precondition이 가드 조건으로 구현되었는가
9. Postcondition이 코드에 반영되었는가
10. 자원 회수와 정상 종료가 구분되었는가

---

## 과제 제출 내용

Step 1, 2, 3의 **prompt** 및 **Use Case Description 파일**

- **발표자료**: `WS1_팀명_시스템명_발표자료.pptx`

### 단계별 제출 항목

| 단계 | 제출 항목 | 발표 포함 내용 |
|------|----------|---------------|
| **Step 1** | 최종 입력 프롬프트<br>`WS1_팀명_시스템명_UseCaseDiagrm.md` | 수정 전 / 수정 후 Use Case Diagram 비교 설명 |
| **Step 2** | 학생 작성 Use Case Description<br>`WS1_팀명_시스템명_UseCaseDescription.md` | • AI 생성 Use Case Description에서 누락·모호 내용 → 수정 내용<br>• Use Case Description 품질검토 내용 |
| **Step 3** | L1 / L2 코드 생성 Prompt<br>`WS1_팀명_시스템명_L1.md`<br>`WS1_팀명_시스템명_L2.md` | L1 코드 / L2 코드 / 구조적 차이 비교표 / 실험 소감 |
