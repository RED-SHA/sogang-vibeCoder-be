# K-MedTour Backend

해외 VIP 환자의 의료 + 관광 여정을 통합 관리하는 B2B/B2C SaaS 백엔드 API 서버

## 프로젝트 개요

| 항목 | 내용 |
|------|------|
| B2B | 에이전시용 통합 관제 대시보드 (환자 여정 관리, 실무자 배정, 실시간 모니터링) |
| B2C | 환자용 다국어 디지털 컨시어지 (실시간 일정표, 담당자 정보, 채팅) |
| Staff | 기사/통역사용 모바일 웹 (스케줄 확인, 상태 보고, 환자 인계) |

---

## 1. 백엔드 아키텍처

### 1.1 기술 스택

```
┌─────────────────────────────────────────────────────┐
│                    Client Layer                      │
│         (Web App / Mobile App / External API)        │
└──────────────────────┬──────────────────────────────┘
                       │ HTTPS / WSS
┌──────────────────────▼──────────────────────────────┐
│                  API Gateway                         │
│              Spring Boot 3.x (Java 21)               │
├──────────────────────────────────────────────────────┤
│  Security    │  WebSocket (STOMP)  │  REST API       │
│  JWT/OAuth2  │  SSE                │  SpringDoc OAS  │
├──────────────┴─────────────────────┴─────────────────┤
│                 Service Layer                        │
│          Business Logic + RBAC + i18n                │
├──────────────────────────────────────────────────────┤
│  Spring Data JPA  │  QueryDSL  │  Redis Cache       │
├───────────────────┴────────────┴─────────────────────┤
│  PostgreSQL       │  AWS S3 / MinIO  │  FCM          │
│  (Flyway 관리)     │  (파일 스토리지)    │  (푸시 알림)   │
└──────────────────────────────────────────────────────┘
```

| 영역 | 기술 | 버전/비고 |
|------|------|----------|
| Language | Java | 21 (record, sealed class, pattern matching) |
| Framework | Spring Boot | 3.x |
| Build | Gradle | Kotlin DSL |
| Database | PostgreSQL | 15+ |
| ORM | Spring Data JPA + QueryDSL | 5.x |
| Auth | Spring Security + OAuth2 (Google, Apple) + JWT + Magic Link | |
| Real-time | WebSocket (STOMP) / SSE | 5초 이내 동기화 |
| API Docs | SpringDoc OpenAPI (Swagger) | |
| Migration | Flyway | |
| Cache | Redis | 7+ |
| File Storage | AWS S3 (또는 MinIO) | |
| Notification | FCM / Web Push | |
| i18n | Spring MessageSource | en / zh / ja / ar |
| Testing | JUnit 5 + Mockito + Testcontainers | |
| CI/CD | GitHub Actions | |

### 1.2 패키지 구조

```
src/main/java/com/k/medtour/
├── KMedTourApplication.java
├── global/                          # 전역 인프라
│   ├── config/                      # Security, WebSocket, Redis, JPA, Swagger
│   ├── exception/                   # GlobalExceptionHandler, BusinessException, ErrorCode
│   ├── auth/                        # JWT Provider/Filter, OAuth2, MagicLink, UserPrincipal
│   ├── i18n/                        # 다국어 메시지 처리
│   └── common/                      # BaseEntity, ApiResponse<T>, PageResponse<T>
├── domain/                          # 비즈니스 도메인 (DDD-lite)
│   ├── admin/                       # 관리자 대시보드
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── entity/
│   │   └── dto/
│   ├── patient/                     # 환자 (온보딩, 프로필)
│   ├── staff/                       # 실무자 (기사/통역)
│   ├── journey/                     # 여정/일정 (핵심 집계 루트)
│   ├── chat/                        # 채팅 + 번역
│   ├── notification/                # 알림 (FCM/WebPush/SSE)
│   ├── proposal/                    # 견적서
│   └── file/                        # 파일 업로드
└── infra/                           # 외부 시스템 연동
    ├── s3/                          # AWS S3 / MinIO
    ├── fcm/                         # Firebase Cloud Messaging
    └── translation/                 # 번역 API
```

### 1.3 핵심 도메인 관계

```
Member (ADMIN | PATIENT | STAFF)
  │
  ├── Patient ──→ Proposal ──→ Journey (핵심 집계 루트)
  │                               │
  │                               ├── JourneySchedule (일정 아이템)
  │                               │     └── StaffAssignment (실무자 배정)
  │                               │
  │                               └── Aftercare (사후 관리)
  │
  ├── Staff ──→ StaffAssignment
  │
  └── ChatRoom ──→ ChatMessage
        └── Notification
```

### 1.4 인증/인가 (RBAC)

```
┌─ Role ──────────────────────────────────────────────┐
│  MASTER    모든 권한                                  │
│  ADMIN     운영 권한 (환자관리, 여정관리, 채팅관제)       │
│  PATIENT   본인 데이터 접근                            │
│  STAFF     배정된 여정 데이터 접근                       │
└─────────────────────────────────────────────────────┘
         │ N:M
┌─ Permission ────────────────────────────────────────┐
│  DASHBOARD_VIEW, PATIENT_MANAGE, STAFF_ASSIGN,      │
│  JOURNEY_MANAGE, CHAT_MONITOR, ...                  │
└─────────────────────────────────────────────────────┘

MVP: @PreAuthorize("hasRole()") → 추후 hasAuthority() 전환 가능
JWT: { sub, role, permissions[] }
```

### 1.5 실시간 통신

```
┌─ WebSocket (STOMP) ─────────────────────────────────┐
│  Subscribe:                                         │
│    /topic/journey/{id}/status    일정 상태 변경        │
│    /topic/journey/{id}/schedule  일정 추가/수정        │
│    /topic/chat/{roomId}          채팅 메시지           │
│    /user/queue/notification      개인 알림            │
│                                                     │
│  Publish:                                           │
│    /app/chat/{roomId}/send       메시지 전송           │
│    /app/staff/status             실무자 상태 업데이트    │
└─────────────────────────────────────────────────────┘

요구사항: 실무자 상태 변경 → 5초 이내 대시보드 + 환자 앱 반영
```

### 1.6 공통 패턴

| 패턴 | 구현 |
|------|------|
| 응답 래핑 | `ApiResponse<T>` (success, data, error) |
| 페이지네이션 | `PageResponse<T>` (content, page, size, totalElements) |
| 예외 처리 | `GlobalExceptionHandler` + `BusinessException` + `ErrorCode` enum |
| 감사(Audit) | `BaseEntity` (id, createdAt, updatedAt, deletedAt) |
| Soft Delete | `deletedAt` 필드 기반 |
| DTO 변환 | Java 21 record + `static from(Entity)` 팩토리 |

---

## 2. 에이전트 아키텍처 (Harness Engineering)

Anthropic의 [Generator-Evaluator 패턴](https://www.anthropic.com/engineering/harness-design-long-running-apps)을 기반으로, 장기간 자율 개발 시 **자기평가 함정**을 방지하고 품질을 보장하는 하네스 구조.

### 2.1 에이전트 구성

```
┌─────────────────────────────────────────────────────┐
│                  Tier 1: PLANNER                     │
│                                                     │
│  sprint-planner    요구사항 → 스프린트 분해 + 계약서    │
│                    코드 작성 안 함. 계획만.              │
└──────────────────────┬──────────────────────────────┘
                       │ contract.md
┌──────────────────────▼──────────────────────────────┐
│                  Tier 2: GENERATORS                   │
│                                                     │
│  project-init      Sprint 0 전용 스캐폴딩             │
│  db-designer       Entity + ERD + Flyway migration   │
│  feature-impl      DTO → Repo → Service → Controller │
│                    + 단위/슬라이스 테스트               │
└──────────────────────┬──────────────────────────────┘
                       │ generator-output.md
┌──────────────────────▼──────────────────────────────┐
│                  Tier 3: EVALUATORS                   │
│                                                     │
│  evaluator         빌드/테스트/계약 검증 + 코드 리뷰    │
│                    ⚠️ 코드 수정 불가 (보고서만 작성)     │
│  build-verifier    경량 컴파일/테스트 체크 (훅용)       │
└──────────────────────┬──────────────────────────────┘
                       │ evaluator-report.md
                       ▼
              PASS → 다음 스프린트
              FAIL → Generator 재호출 (수정 사이클)
```

### 2.2 파일 기반 통신

에이전트 간 컨텍스트는 **파일**로 전달됩니다. 각 스프린트마다 3개의 계약 문서가 생성됩니다.

```
docs/sprints/
├── sprint-plan.md                    # 마스터 플랜 (전체 스프린트 목록)
├── sprint-0/
│   ├── contract.md                   # Planner → 완료 기준 31개 체크리스트
│   ├── generator-output.md           # Generator → 생성된 파일 목록 + 결정사항
│   └── evaluator-report.md           # Evaluator → PASS/FAIL + 발견사항
├── sprint-1/
│   ├── contract.md
│   ├── generator-output.md
│   └── evaluator-report.md
└── ...
```

| 파일 | 작성자 | 독자 | 내용 |
|------|-------|------|------|
| `contract.md` | sprint-planner | db-designer, feature-impl, evaluator | 범위, 엔드포인트, 수락 기준 체크리스트 |
| `generator-output.md` | db-designer, feature-impl | evaluator | 생성/수정된 파일 목록, 아키텍처 결정 |
| `evaluator-report.md` | evaluator | feature-impl (수정 사이클) | 빌드/테스트 결과, Critical/Warning 목록 |

### 2.3 스프린트 계획

| Sprint | 범위 | 요구사항 | 엔드포인트 | 의존성 |
|--------|------|---------|----------|-------|
| 0 | Foundation (프로젝트 세팅) | 0 | 0 | - |
| 1 | Auth + Member + RBAC | 9 | ~14 | Sprint 0 |
| 2 | Patient Onboarding | 4 | ~11 | Sprint 1 |
| 3 | File Upload (cross-cutting) | 3 | ~3 | Sprint 1 |
| 4 | Proposal/Quotation | 4 | ~8 | Sprint 2 |
| 5 | Journey Core (핵심) | 10 | ~17 | Sprint 2 |
| 6 | Chat + Translation | 6 | ~8 | Sprint 1, 3 |
| 7 | Notification + Dashboard + Aftercare | 10 | ~15 | Sprint 5 |

**총 43개 요구사항, ~76개 엔드포인트**

### 2.4 스프린트 실행 워크플로우

```
┌─────────────────────────────────────────────────────┐
│ 1. sprint-planner → contract.md 생성                 │
│ 2. 사용자 계약 검토/수정                               │
│ 3. db-designer → Entity + Flyway migration           │
│ 4. feature-impl → DTO → Repo → Service → Controller │
│                   + Service Test + Controller Test    │
│ 5. evaluator → 빌드/테스트/계약 검증                    │
│    ├─ PASS → git commit → push (스프린트 종료 시)      │
│    └─ FAIL → feature-impl 재호출 (수정 사이클)         │
│              → 5번으로 돌아감                          │
└─────────────────────────────────────────────────────┘
```

### 2.5 평가 기준 (31개)

| 카테고리 | 항목 수 | 주요 내용 |
|---------|--------|----------|
| 빌드 | 4 | compileJava 성공, test 통과, deprecated 경고 없음, import 해결 |
| 아키텍처 | 6 | 계층 분리, ApiResponse 래핑, BaseEntity 상속, @PreAuthorize |
| 보안 | 5 | 소유권 검증, 2FA, 파일 검증, 에러 메시지 ID 노출 금지 |
| 테스트 | 5 | Service/Controller 테스트, @DisplayName 한국어, Given-When-Then |
| 코드 품질 | 4 | record DTO, 커스텀 예외, 로깅, 상수/Enum |
| DB | 4 | Flyway, LAZY 로딩, 인덱스, soft delete |
| 실시간 | 3 | WebSocket 토픽, 5초 동기화, STOMP 포맷 |

### 2.6 자동화 훅

| 훅 | 이벤트 | 동작 |
|----|-------|------|
| Prompt Logger | `UserPromptSubmit` | 모든 프롬프트를 `prompt/YYYYMMDD_HH.json`에 JSON 저장 |
| Build Verifier | `PostToolUse` (Bash) | `git commit` 감지 시 `./gradlew compileJava` 자동 실행 |

---

## 3. Git 전략

### 3.1 브랜치 전략 (Gitflow)

```
main ─────────────────────────────────────────────────
  │
  └── develop ────────────────────────────────────────
        │                         │
        ├── feature/sprint-0-init │
        │     ├── commit: "chore: init Spring Boot project"
        │     ├── commit: "feat: add BaseEntity, ApiResponse"
        │     ├── commit: "feat: add SecurityConfig stub"
        │     └── merge → develop (Sprint 0 완료)
        │
        ├── feature/sprint-1-auth
        │     ├── commit: "feat(auth): add JWT provider"
        │     ├── commit: "feat(auth): add OAuth2 login"
        │     ├── commit: "feat(member): add Member entity"
        │     ├── commit: "test(auth): add JwtTokenProvider unit test"
        │     └── merge → develop (Sprint 1 완료)
        │
        ├── feature/sprint-2-patient
        │     └── ...
        │
        └── release/v1.0.0 → main (MVP 릴리스)
```

### 3.2 브랜치 규칙

| 브랜치 | 목적 | 네이밍 |
|--------|------|--------|
| `main` | 프로덕션 릴리스 | - |
| `develop` | 개발 통합 | - |
| `feature/*` | 스프린트 단위 기능 개발 | `feature/sprint-{N}-{short-desc}` |
| `release/*` | 릴리스 준비 | `release/v{major}.{minor}.{patch}` |
| `hotfix/*` | 긴급 수정 | `hotfix/{issue-desc}` |

### 3.3 커밋 컨벤션

**Conventional Commits** + 잘게 쪼개기

```
feat(domain): 기능 추가 설명
fix(domain): 버그 수정 설명
chore: 빌드/설정 변경
test(domain): 테스트 추가/수정
docs: 문서 변경
refactor(domain): 리팩토링
```

**커밋 단위 원칙**: 하나의 커밋 = 하나의 논리적 변경
- Entity 생성 → 커밋
- Repository 추가 → 커밋
- Service 구현 → 커밋
- Controller 구현 → 커밋
- 테스트 추가 → 커밋
- Flyway 마이그레이션 → 커밋

**푸시 타이밍**: 스프린트 종료 시 (evaluator PASS 후) feature 브랜치 → develop merge & push

### 3.4 스프린트별 Git 플로우

```
1. develop에서 feature/sprint-{N}-{desc} 브랜치 생성
2. 잘게 쪼개서 커밋 (Entity → Repo → Service → Controller → Test)
3. evaluator PASS 확인
4. feature → develop 머지
5. develop push
6. 다음 스프린트 반복
```

---

## 4. 요구사항 요약

### 관리자 (ADM) - 14개
| ID | 기능 | 우선순위 |
|----|------|---------|
| ADM-101 | 대시보드 Overview | P1 필수 |
| ADM-102 | 긴급 알림 Alert Center | P1 필수 |
| ADM-201 | 환자 DB/서류 검토 | P1 필수 |
| ADM-301 | 스마트 견적서 생성 | P1 필수 |
| ADM-302 | 에이전시/병원 프로필 관리 | P2 필수 |
| ADM-401 | 여정 템플릿 빌더 CMS | P1 필수 |
| ADM-402 | 실시간 일정 수정/동기화 | P1 필수 |
| ADM-501 | 통합 채팅 관제 Multi-Chat | P1 필수 |
| ADM-502 | 자동 번역 지원 | P1 필수 |
| ADM-601 | 실무자 자동 배정 | P1 필수 |
| ADM-602 | 실무자 실시간 위치/상태 | P2 옵션 |
| ADM-701 | 사후 관리 가이드 배포 | P1 필수 |
| ADM-702 | 정산/인보이스 발행 | P2 필수 |
| ADM-801 | RBAC 권한 관리 | P1 필수 |

### 환자 (PAT) - 18개
| ID | 기능 | 우선순위 |
|----|------|---------|
| PAT-101~103 | 회원가입/OAuth/매직링크 | P1 |
| PAT-201~203 | 온보딩 (여권/문진표/긴급연락처) | P1 |
| PAT-301~303 | 프로필 조회 (라이선스/포트폴리오/실무자) | P1~P2 |
| PAT-401~403 | 견적 요청/비교/수락 | P1 |
| PAT-501~503 | 채팅/파일전송/시스템알림 | P1 |
| PAT-601~604 | 일정표/실시간동기화/푸시/지도 | P1~P2 |
| PAT-701~702 | 사후관리/인보이스 | P1~P2 |

### 실무자 (STA) - 11개
| ID | 기능 | 우선순위 |
|----|------|---------|
| STA-101~102 | 로그인/프로필 | P1 |
| STA-201~202 | 업무리스트/배정알림 | P1 |
| STA-301 | 환자 특이사항 조회 | P1 |
| STA-401~402 | 상태업데이트/사진업로드 | P1~P2 |
| STA-501 | 지도 딥링크 | P1 |
| STA-601~602 | 채팅/SOS | P1~P2 |
| STA-701 | 업무종료 리포트 | P2 |

---

## 5. 프로젝트 실행

```bash
# 인프라 기동
docker-compose up -d

# 빌드 및 실행
./gradlew bootRun --args='--spring.profiles.active=local'

# 테스트
./gradlew test

# API 문서
open http://localhost:8080/swagger-ui.html
```

---

## 6. 참고 링크

| 항목 | URL |
|------|-----|
| 노션 기획서 | https://storedh.notion.site/K-326bdb99cba58013a75dd398ef69e605 |
| API 명세서 | [`docs/api-spec.md`](docs/api-spec.md) |
| 관리자 프로토타입 | [Stitch Preview (Admin)](https://stitch.withgoogle.com/preview/14414138817315747256?node-id=76a23d34b33847728ef8c38209984503) |
| 환자 프로토타입 | [Stitch Preview (Patient)](https://stitch.withgoogle.com/preview/14414138817315747256?node-id=1b4f6e6a351e43ff886cba2671dd8e45) |
| 피그마 | [Figma Design](https://www.figma.com/design/OONDRiBb95Io9qlJX0QKKM) |
