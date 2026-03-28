# Sprint 7 Contract: Notification + Dashboard + Aftercare

## 범위

### 요구사항
- ADM-101: 대시보드 Overview
- ADM-102: 긴급 알림 Alert Center
- ADM-701: 사후 관리 가이드 배포
- ADM-702: 정산/인보이스 발행
- PAT-503: 시스템 알림 메시지
- PAT-603: 일정 변동 시 푸시 알림
- PAT-701: 사후 관리 가이드
- PAT-702: 영수증/인보이스 내역
- STA-202: 배정 알림 푸시
- STA-701: 업무 종료 리포트
- ADM-602: 실무자 실시간 위치/상태
- PAT-302: 병원 포트폴리오

### 구현할 엔드포인트 (~15개)

#### Notification (6개)
- GET /api/v1/notifications — 내 알림 목록
- POST /api/v1/notifications/{id}/read — 알림 읽음
- POST /api/v1/notifications/read-all — 전체 읽음
- GET /api/v1/notifications/unread-count — 미읽은 수
- GET /api/v1/notifications/admin/alerts — 긴급 알림 (ADMIN)
- POST /api/v1/notifications/send — 알림 발송 (ADMIN)

#### Dashboard (2개)
- GET /api/v1/dashboard/overview — 대시보드 Overview
- GET /api/v1/dashboard/staff-status — 실무자 상태 모니터링

#### Aftercare (7개)
- POST /api/v1/aftercare/guides — 사후 관리 가이드 생성 (ADMIN)
- GET /api/v1/aftercare/guides/{journeyId} — 가이드 조회
- POST /api/v1/aftercare/invoices — 인보이스 생성 (ADMIN)
- GET /api/v1/aftercare/invoices/{journeyId} — 인보이스 조회
- GET /api/v1/aftercare/invoices/me — 내 인보이스 (PATIENT)
- POST /api/v1/aftercare/reports — 업무 종료 리포트 (STAFF)
- GET /api/v1/profiles/organizations/{id}/portfolio — 포트폴리오

### Entity
- Notification, AftercareGuide, Invoice, InvoiceItem, StaffReport

## 수락 기준
- [ ] compileJava + compileTestJava 성공
- [ ] 각 도메인 Service/Controller 테스트
- [ ] Flyway V8
- [ ] 대시보드 통계 집계 쿼리
- [ ] record DTO

## 예상 파일 수: ~35개
## 의존성: Sprint 5 완료
