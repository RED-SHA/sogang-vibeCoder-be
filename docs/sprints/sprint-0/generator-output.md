# Sprint 0 Generator Output

## Created Files

| File Path | Type | Description |
|-----------|------|-------------|
| build.gradle.kts | Build | Spring Boot 3.4.4, Java 21, 전체 의존성 |
| settings.gradle.kts | Build | 프로젝트명 k-medtour |
| gradle.properties | Build | JVM 옵션, Java 21 home |
| .gitignore | Config | Gradle, IDE, 환경 파일 제외 |
| gradlew, gradlew.bat, gradle/ | Build | Gradle Wrapper 8.12 |
| docker-compose.yml | Infra | PostgreSQL 15 + Redis 7 |
| .env.example | Infra | 환경변수 템플릿 |
| KMedTourApplication.java | App | Spring Boot 메인 클래스 |
| BaseEntity.java | Common | id, createdAt, updatedAt, deletedAt |
| ApiResponse.java | Common | success(), error() 정적 팩토리 |
| PageResponse.java | Common | Page -> PageResponse 변환 |
| ErrorCode.java | Exception | 도메인별 에러 코드 enum |
| BusinessException.java | Exception | 커스텀 비즈니스 예외 |
| ErrorResponse.java | Exception | 에러 응답 record |
| GlobalExceptionHandler.java | Exception | 전역 예외 처리 |
| JwtProperties.java | Auth | JWT 설정 record |
| JwtTokenProvider.java | Auth | 토큰 생성/검증 |
| JwtAuthenticationFilter.java | Auth | Security Filter |
| UserPrincipal.java | Auth | 인증 주체 record |
| SecurityConfig.java | Config | CORS, CSRF, 세션, 필터 체인 |
| JpaConfig.java | Config | JPA Auditing 활성화 |
| WebSocketConfig.java | Config | STOMP 브로커, 엔드포인트 |
| RedisConfig.java | Config | RedisTemplate 설정 |
| WebConfig.java | Config | WebMvc 설정 (확장용) |
| SwaggerConfig.java | Config | OpenAPI + JWT Bearer 인증 |
| application.yml | Resource | 공통 설정 |
| application-local.yml | Resource | 로컬 개발 DB/Redis |
| application-dev.yml | Resource | 개발 환경 |
| application-prod.yml | Resource | 운영 환경 |
| messages.properties | i18n | 영어 (기본) |
| messages_zh.properties | i18n | 중국어 |
| messages_ja.properties | i18n | 일본어 |
| messages_ar.properties | i18n | 아랍어 |
| V1__init_auth_tables.sql | Migration | role, permission, role_permission, member |

## Decisions Made
- Java 25가 기본 설치되어 있으나 Gradle 8.12 호환을 위해 gradle.properties에 Java 21 home 명시
- SecurityConfig MVP: /api/v1/auth/**, /swagger-ui/** 등 인증 제외 경로 설정, 나머지 authenticated
- RBAC 초기 데이터: MASTER(전체), ADMIN(운영), STAFF(최소), PATIENT(파일만) 권한 할당

## Ready for Evaluation: YES
