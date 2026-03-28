# Sprint 5 Contract: Journey Core

## 범위

### 요구사항
- ADM-401: 여정 템플릿 빌더 CMS
- ADM-402: 실시간 일정 수정/동기화
- ADM-601: 실무자 자동 배정
- PAT-601: 라이브 타임라인
- PAT-602: 일정 상태 실시간 동기화
- PAT-604: Google Maps 딥링크
- STA-201: 당일 업무 리스트
- STA-301: 환자 특이사항 조회
- STA-401: 원터치 상태 업데이트
- STA-501: 지도 딥링크

### 구현할 엔드포인트 (17개)

#### 템플릿 관리 (5개, ADMIN)
- GET /api/v1/journeys/templates — 템플릿 목록
- POST /api/v1/journeys/templates — 템플릿 생성
- GET /api/v1/journeys/templates/{id} — 템플릿 상세
- PUT /api/v1/journeys/templates/{id} — 템플릿 수정
- DELETE /api/v1/journeys/templates/{id} — 템플릿 삭제

#### 여정 관리 (4개)
- POST /api/v1/journeys — 여정 생성 (ADMIN)
- GET /api/v1/journeys — 여정 목록 (ADMIN)
- GET /api/v1/journeys/{id} — 여정 상세 (ALL)
- POST /api/v1/journeys/{id}/assign-staff — 실무자 배정 (ADMIN)

#### 일정 항목 (3개, ADMIN)
- POST /api/v1/journeys/{id}/schedule-items — 일정 추가
- PUT /api/v1/journeys/{id}/schedule-items/{itemId} — 일정 수정
- DELETE /api/v1/journeys/{id}/schedule-items/{itemId} — 일정 삭제

#### 환자 (1개)
- GET /api/v1/journeys/me/timeline — 라이브 타임라인

#### 실무자 (4개)
- GET /api/v1/journeys/staff/me/today — 당일 업무
- GET /api/v1/journeys/{id}/patient-notice — 환자 특이사항
- PATCH /api/v1/journeys/{id}/schedule-items/{itemId}/status — 상태 업데이트
- GET /api/v1/journeys/{id}/schedule-items/{itemId}/navigation — 지도 딥링크

### Entity
- JourneyTemplate, JourneyTemplateItem
- Journey, JourneyScheduleItem (location JSONB)
- StaffAssignment

### Enum
- JourneyStatus (PLANNED, IN_PROGRESS, COMPLETED, CANCELLED)
- ScheduleItemType (TRANSPORT, MEDICAL, ACCOMMODATION, TOUR, CUSTOM)
- ScheduleItemStatus (SCHEDULED, EN_ROUTE, ARRIVED, IN_PROGRESS, COMPLETED, CANCELLED)
- StaffAssignmentStatus (ASSIGNED, ON_DUTY, COMPLETED)
- TemplateCategory (SURGERY, TOUR, RECOVERY, MIXED)

## 수락 기준
- [ ] compileJava + compileTestJava 성공
- [ ] JourneyService, StaffAssignment 단위 테스트
- [ ] JourneyController 슬라이스 테스트
- [ ] 상태 전이 검증
- [ ] 접근 제어 (PATIENT 본인, STAFF 배정된 여정)
- [ ] Google Maps 딥링크 생성
- [ ] Flyway V6

## 예상 파일 수: ~40개
## 의존성: Sprint 2 완료
