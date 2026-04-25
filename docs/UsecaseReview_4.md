# Use Case Diagram Review #4 — K-Medical Concierge OS

> 대상: `docs/UseCaseDiagram.puml` (rev. 5)
> 입력 비교: `docs/requirements.md`, `docs/UseCaseModeling.md`, `docs/UseCaseModelingRules.md`
> 이전 iteration: `docs/UsecaseReview_3.md`
> Iteration: 4
> 생성: 2026-04-25

---

## 1. 이전 Iteration 조치 결과 확인

| 이전 ID | 내용 | 적용 여부 |
|--------|------|----------|
| P-06 | Hotel/Airline Partner Actor의 UC 직접 연결 누락 | ✅ 해소 (Partner Actor 전부 제거 — 사용자 명시 결정) |

> 사용자 결정에 따라 Hotel/Airline Partner를 다이어그램에서 제외. P-04~P-06 모두 자동 해소. PD §3 명시 5개 주체 중 항공·숙박은 추적성표에서도 제거됨.

---

## 2. 검토 항목별 결과

| # | 검토 항목 | 결과 | 비고 |
|---|----------|------|------|
| 1 | Actor 수가 Problem Description 행위자 목록과 일치 | ✅ 합격 (조건부) | PD §3 5개 주체 중 항공·숙박은 사용자 명시 결정으로 제외, 병원은 Trade-off §8 제외. 기사·통역사·환자·관리자만 반영 |
| 2 | 모든 Actor가 Human / External System 구분 | ✅ 합격 | Human 6 / External 8, `<<external>>` 적용 |
| 3 | Start Up / Shut Down UC가 Operator Actor와 연결 | ✅ 합격 | `OP --> UC_OP_01`, `OP --> UC_OP_02` |
| 4 | 반복 공통 흐름이 Abstract UC로 분리되어 «abstract» 표시 | ✅ 합격 | 6개 모두 다수 Concrete 호출 |
| 5 | «include» 방향 Concrete → Abstract | ✅ 합격 | 24건 모두 올바른 방향 |
| 6 | «extend» 방향 Extension → Base | ✅ 합격 | 4건 모두 올바른 방향 |
| 7 | 모든 Actor ≥1 UC 연결 | ✅ 합격 | Human 6 + External 8 = 14 모두 UC와 직접 연결 |
| 8 | 고립 UC 없음 | ✅ 합격 | 모든 UC 결합됨 |
| 9 | UC 이름이 동사+명사 형태 | ✅ 합격 | 모든 UC 동사 시작 |
| 10 | System Boundary 명시 | ✅ 합격 | `rectangle "K-Medical Concierge OS"` |

**총평**: **10 합격 / 0 부분 / 0 위반**. 모든 검토 항목 통과. WS1 종료 조건 충족.

---

## 3. 발견된 문제점

### [P-07] (정보, 미적용) PD §3 항공·숙박 행위자 명시 제외 근거 문서화 권장

- **내용**: requirements.md §3 Problem Statements는 "항공, 숙박, 병원 예약, 기사, 통역사 5개 주체"를 명시하지만, 현재 Use Case Diagram에는 항공·숙박이 표현되지 않음.
- **현재 처리**: 사용자 명시 결정에 의한 제외 (rev. 5).
- **위반 여부**: 룰 1·5 표면 해석상 위반 아님 (사용자 결정 우선). 단 추적성 측면에서 근거 명시 부재.
- **권장**: requirements.md §8 Trade-offs에 "항공·숙박 외부 의존 미반영" 항목 추가 또는 UseCaseModeling.md §2-4 미생성 항목 표에 명시. **다이어그램 변경 불필요**.

---

## 4. 수정 PlantUML 코드

> **변경 사항 없음**. 모든 검토 항목 통과. 현재 `docs/UseCaseDiagram.puml` (rev. 5) 그대로 유지.

```plantuml
' 현재 파일 상태 (rev. 5) 그대로 사용.
' 핵심 구성:
'   - Human Actor 6: Operator, Master Admin, Agency Admin, Registered Patient, Guest Patient, Field Staff
'   - External Actor 8 (<<external>>): OAuth Provider, Magic Link Provider, OCR Service,
'                                       Translation Engine, Realtime Sync Bus, Push Notification Gateway,
'                                       Encrypted Storage, Map Service
'   - System Boundary: rectangle "K-Medical Concierge OS"
'   - Concrete UC 38, Abstract UC 6 («abstract»)
'   - <<include>> 24, <<extend>> 4
'   - UC ↔ External 의존 4 + Abstract ↔ External 의존 6
```

---

## 5. 다음 Iteration 권장 작업

1. **WS1 종료**: 모든 검토 항목 ✅. 다이어그램·모델링 문서 freeze 후 WS2 (예: 도메인 모델링, 시퀀스 다이어그램, API 명세) 단계 진행.
2. P-07 정보성 권장: requirements.md §8 Trade-offs에 항공·숙박 미반영 근거 1줄 추가 (선택).
3. 추가 iteration 불필요. 새 요구사항 추가 시 `UsecaseReview_5.md`로 재개.
