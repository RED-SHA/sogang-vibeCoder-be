# K-의료 관광 솔루션 (Backend)

## 프로젝트 개요

해외 VIP 환자의 의료와 관광 여정을 통합 관리하는 B2B/B2C SaaS 솔루션의 **백엔드 API 서버**.
- **B2B**: 에이전시용 통합 관제 대시보드 API (환자 여정 관리, 실무자 배정, 실시간 모니터링)
- **B2C**: 환자용 다국어 디지털 컨시어지 API (실시간 일정표, 담당자 정보, 채팅)
- **실무자**: 기사/통역사용 모바일 웹 API (스케줄 확인, 상태 보고, 환자 인계)

## 기술 스택

| 영역 | 기술 |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.x |
| Build | Gradle (Kotlin DSL) |
| Database | PostgreSQL |
| ORM | Spring Data JPA + QueryDSL |
| Auth | Spring Security + OAuth2 (Google, Apple) + JWT + Magic Link |
| Real-time | WebSocket (STOMP) / SSE |
| API Docs | SpringDoc OpenAPI (Swagger) |
| Migration | Flyway |
| Cache | Redis |
| File Storage | AWS S3 (또는 MinIO) |
| Notification | Firebase Cloud Messaging (FCM) / Web Push |
| i18n | Spring MessageSource (최소 영/중/일/아랍어) |
| Testing | JUnit 5 + Mockito + Testcontainers |
| CI/CD | GitHub Actions |

## 프로젝트 구조

```
/
├── src/main/java/com/k/medtour/
│   ├── KMedTourApplication.java
│   ├── global/                  # 전역 설정, 예외, 보안, 유틸
│   │   ├── config/              # SecurityConfig, WebSocketConfig, RedisConfig 등
│   │   ├── exception/           # GlobalExceptionHandler, 커스텀 예외
│   │   ├── auth/                # JWT, OAuth2, MagicLink 인증/인가
│   │   ├── i18n/                # 다국어 메시지 처리
│   │   └── common/              # BaseEntity, ApiResponse, PageResponse 등
│   ├── domain/
│   │   ├── admin/               # 관리자 도메인
│   │   │   ├── controller/
│   │   │   ├── service/
│   │   │   ├── repository/
│   │   │   ├── entity/
│   │   │   └── dto/
│   │   ├── patient/             # 환자 도메인
│   │   ├── staff/               # 실무자 도메인
│   │   ├── journey/             # 여정/일정 도메인 (핵심)
│   │   ├── chat/                # 채팅 도메인
│   │   ├── notification/        # 알림 도메인
│   │   ├── proposal/            # 견적서 도메인
│   │   └── file/                # 파일 업로드 도메인
│   └── infra/                   # 외부 연동 (S3, FCM, 번역 API 등)
├── src/main/resources/
│   ├── application.yml
│   ├── application-{profile}.yml
│   ├── messages/                # i18n 메시지 번들
│   └── db/migration/            # Flyway 마이그레이션
├── src/test/
├── build.gradle.kts
└── settings.gradle.kts
```

## 대상 사용자 & 핵심 도메인

### 1. 관리자 (Admin) API

| Req ID | 카테고리 | 기능 | 우선순위 | 필수 |
|--------|--------|------|---------|------|
| ADM-101 | 대시보드 | 운영 현황 요약 (Overview) | P1 | 필수 |
| ADM-102 | 대시보드 | 긴급 상황 알림 (Alert Center) | P1 | 필수 |
| ADM-201 | 환자 관리 | 환자 DB 및 서류 검토 (여권 OCR, 의료 문진표) | P1 | 필수 |
| ADM-301 | 견적/제청 | 스마트 견적서(Proposal) 생성 | P1 | 필수 |
| ADM-302 | 견적/제청 | 에이전시/병원 프로필 관리 | P2 | 필수 |
| ADM-401 | 일정표 관리 | 여정 템플릿 빌더 (CMS) | P1 | 필수 |
| ADM-402 | 일정표 관리 | 실시간 일정 수정 및 동기화 | P1 | 필수 |
| ADM-501 | 커뮤니케이션 | 통합 채팅 관제 (Multi-Chat) | P1 | 필수 |
| ADM-502 | 커뮤니케이션 | 자동 번역 지원 대화창 | P1 | 필수 |
| ADM-601 | 자원 배정 | 실무자(기사/통역) 자동 배정 | P1 | 필수 |
| ADM-602 | 자원 배정 | 실무자 실시간 위치/상태 모니터링 | P2 | 옵션 |
| ADM-701 | 사후 관리 | 사후 관리 가이드 배포 | P1 | 필수 |
| ADM-702 | 사후 관리 | 정산 및 인보이스 발행 | P2 | 필수 |
| ADM-801 | 설정/보안 | 권한 관리 (RBAC) | P1 | 필수 |

### 2. 환자 (Patient) API

| Req ID | 카테고리 | 기능 | 우선순위 | 필수 |
|--------|--------|------|---------|------|
| PAT-101 | 회원가입 | 글로벌 소셜 로그인 (OAuth) | P1 | 필수 |
| PAT-102 | 회원가입 | 글로벌 규제 동의 (T&C, Privacy) | P1 | 필수 |
| PAT-103 | 회원가입 | 비회원 매직 링크 (Guest View) | P1 | 옵션 |
| PAT-201 | 온보딩 | 여권 정보 업로드 (Manual/OCR) | P1 | 필수 |
| PAT-202 | 온보딩 | 영문 의료/알레르기 문진표 | P1 | 필수 |
| PAT-203 | 온보딩 | 긴급 연락처 등록 | P1 | 필수 |
| PAT-301 | 프로필 조회 | 에이전시/병원 라이선스 검증 | P1 | 필수 |
| PAT-302 | 프로필 조회 | 병원 포트폴리오 (Before & After) | P2 | 옵션 |
| PAT-303 | 프로필 조회 | 배정된 실무자 프로필 | P1 | 필수 |
| PAT-401 | 견적 | 수술/컨시어지 견적 요청 | P1 | 필수 |
| PAT-402 | 견적 | 견적서 수신 및 비교 | P1 | 필수 |
| PAT-403 | 견적 | 견적 수락 및 기승인 | P1 | 필수 |
| PAT-501 | 채팅 | 에이전시 1:1 양문 텍스트 채팅 | P1 | 필수 |
| PAT-502 | 채팅 | 의료 사진 및 문서 보안 전송 | P1 | 필수 |
| PAT-503 | 채팅 | 시스템 알림 메시지 | P1 | 필수 |
| PAT-601 | 일정표 | 라이브 타임라인 (Live Itinerary) | P1 | 필수 |
| PAT-602 | 일정표 | 일정 상태 실시간 동기화 | P1 | 필수 |
| PAT-603 | 일정표 | 일정 변동 시 푸시 알림 | P1 | 필수 |
| PAT-604 | 일정표 | Google Maps 딥링크 연동 | P2 | 필수 |
| PAT-701 | 기록 조회 | 수술 후 사후 관리 가이드 | P1 | 필수 |
| PAT-702 | 기록 조회 | 영수증 및 인보이스 내역 | P2 | 필수 |

### 3. 실무자 (Staff) API

| Req ID | 카테고리 | 기능 | 우선순위 | 필수 |
|--------|--------|------|---------|------|
| STA-101 | 온보딩 | 간편 로그인 (Magic Link/SNS) | P1 | 필수 |
| STA-102 | 온보딩 | 실무자 프로필 관리 | P1 | 필수 |
| STA-201 | 일정 관리 | 당일 업무 리스트 (To-do) | P1 | 필수 |
| STA-202 | 일정 관리 | 배정 알림 푸시 (Assignment) | P1 | 필수 |
| STA-301 | 환자 정보 | 환자 특이사항 조회 (Notice) | P1 | 필수 |
| STA-401 | 현장 보고 | 원터치 상태 업데이트 (Status) | P1 | 필수 |
| STA-402 | 현장 보고 | 현장 사진 업로드 (Proof) | P2 | 필수 |
| STA-501 | 길찾기 | 지도 앱 딥링크 연동 | P1 | 필수 |
| STA-601 | 커뮤니케이션 | 에이전시/환자 1:1 채널 | P1 | 필수 |
| STA-602 | 커뮤니케이션 | 긴급 호출 (SOS) | P2 | 옵션 |
| STA-701 | 정산/평가 | 업무 종료 리포트 | P2 | 필수 |

## Acceptance Criteria (핵심)

1. **다국어 & 정보 보안**: 매직 링크는 접속 기기 언어 설정에 맞춰 최소 3개 국어 자동 렌더링. URL 유출 시 예약자 생년월일 등 2차 인증 필수.
2. **실시간 상태 동기화**: 실무자 상태 변경 → 관리자 대시보드 + 환자 웹에 새로고침 없이 5초 이내 반영 (WebSocket/SSE).
3. **데이터 정합성**: 관리자 일정 수정 시 관련 실무자 전원에게 즉각 알림 발송, DB 타임라인 오차 없이 업데이트.

## 개발 컨벤션

- Java 21 (record, sealed class, pattern matching 적극 활용)
- Spring Boot 3.x + Spring Security 6.x
- 커밋 메시지: Conventional Commits (feat/fix/chore/docs)
- 브랜치: feature/{req-id}-{short-desc} (예: feature/ADM-101-dashboard-overview)
- REST API: RESTful 설계, 응답은 공통 ApiResponse<T> 래핑
- 예외 처리: GlobalExceptionHandler + 커스텀 비즈니스 예외
- DTO ↔ Entity 변환: MapStruct 또는 record 기반 수동 매핑
- DB 마이그레이션: Flyway (V{version}__{description}.sql)
- 테스트: 핵심 비즈니스 로직 JUnit 5 단위 테스트, 통합 테스트는 Testcontainers

## 권한 관리 (RBAC) 전략

**MVP**: Role 기반 `@PreAuthorize("hasRole()")` 으로 시작하되, **DB는 Permission 기반으로 설계**하여 추후 세분화 가능한 구조.

### DB 설계 (Permission 확장 대비)
```
Role (역할)
├── id, name, description
├── 초기값: MASTER, ADMIN, PATIENT, STAFF
└── 추후: MANAGER 등 추가 가능

Permission (권한)
├── id, name, description
└── 예: DASHBOARD_VIEW, PATIENT_MANAGE, STAFF_ASSIGN, JOURNEY_MANAGE, CHAT_MONITOR, ...

RolePermission (역할-권한 매핑, N:M)
├── role_id, permission_id
└── MASTER는 모든 권한, ADMIN은 운영 권한, STAFF는 최소 권한

Member (사용자)
├── role 필드로 역할 보유
└── 추후 UserRole N:M으로 전환 가능
```

### MVP 구현 방식
- **API 레벨**: `@PreAuthorize("hasRole('ADMIN')")` — 단순 Role 체크
- **데이터 레벨**: Service에서 `SecurityContext`의 userId로 본인 데이터 검증
  - 환자: 본인 여정/문진표/채팅만 접근
  - 실무자: 배정된 여정만 접근
  - 관리자: 전체 접근
- **JWT Payload**: `{ sub, role, permissions[] }` — permissions는 Role에서 조회하여 포함

### 추후 확장
- `@PreAuthorize("hasAuthority('STAFF_ASSIGN')")` Permission 단위 제어로 전환
- 에이전시별 커스텀 역할 생성 (MANAGER 등)
- Multi-tenancy 지원 시 Organization별 역할 분리

## Trade-offs (MVP 범위)

- **EMR 연동 제외**: 병원 의료 시스템과의 연동은 하지 않음. 의료 행위 외부의 컨시어지에 집중.
- **결제 기능 제외**: 글로벌 PG, 에스크로 등 결제 관련 기능은 MVP에서 완전 배제.
- **프론트엔드 별도**: 이 프로젝트는 백엔드 API만 구현. 프론트엔드는 별도 프로젝트.

## 참고 링크

- 노션 기획서: https://storedh.notion.site/K-326bdb99cba58013a75dd398ef69e605
- 관리자 프로토타입: https://stitch.withgoogle.com/preview/14414138817315747256?node-id=76a23d34b33847728ef8c38209984503
- 환자 프로토타입: https://stitch.withgoogle.com/preview/14414138817315747256?node-id=1b4f6e6a351e43ff886cba2671dd8e45
- 피그마: https://www.figma.com/design/OONDRiBb95Io9qlJX0QKKM
