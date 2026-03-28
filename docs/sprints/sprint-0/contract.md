# Sprint 0 Contract: Foundation (프로젝트 초기 세팅)

## 범위

### 요구사항
- 없음 (인프라 세팅 스프린트)

### 목표
Spring Boot 프로젝트 골격을 생성하여 Sprint 1부터 비즈니스 로직 구현이 가능하도록 한다.

### 생성할 파일 목록

#### 빌드 설정
- `build.gradle.kts`
- `settings.gradle.kts`
- `gradle.properties`
- `.gitignore`

#### 메인 애플리케이션
- `src/main/java/com/k/medtour/KMedTourApplication.java`

#### Global Config
- `src/main/java/com/k/medtour/global/config/SecurityConfig.java`
- `src/main/java/com/k/medtour/global/config/WebSocketConfig.java`
- `src/main/java/com/k/medtour/global/config/RedisConfig.java`
- `src/main/java/com/k/medtour/global/config/JpaConfig.java`
- `src/main/java/com/k/medtour/global/config/WebConfig.java`
- `src/main/java/com/k/medtour/global/config/SwaggerConfig.java`

#### Global Exception
- `src/main/java/com/k/medtour/global/exception/BusinessException.java`
- `src/main/java/com/k/medtour/global/exception/ErrorCode.java`
- `src/main/java/com/k/medtour/global/exception/ErrorResponse.java`
- `src/main/java/com/k/medtour/global/exception/GlobalExceptionHandler.java`

#### Global Auth
- `src/main/java/com/k/medtour/global/auth/jwt/JwtTokenProvider.java`
- `src/main/java/com/k/medtour/global/auth/jwt/JwtAuthenticationFilter.java`
- `src/main/java/com/k/medtour/global/auth/jwt/JwtProperties.java`
- `src/main/java/com/k/medtour/global/auth/UserPrincipal.java`

#### Global Common
- `src/main/java/com/k/medtour/global/common/BaseEntity.java`
- `src/main/java/com/k/medtour/global/common/ApiResponse.java`
- `src/main/java/com/k/medtour/global/common/PageResponse.java`

#### Resources
- `src/main/resources/application.yml`
- `src/main/resources/application-local.yml`
- `src/main/resources/application-dev.yml`
- `src/main/resources/application-prod.yml`
- `src/main/resources/messages/messages.properties`
- `src/main/resources/messages/messages_zh.properties`
- `src/main/resources/messages/messages_ja.properties`
- `src/main/resources/messages/messages_ar.properties`

#### DB Migration
- `src/main/resources/db/migration/V1__init_auth_tables.sql`

#### Infrastructure
- `docker-compose.yml`
- `.env.example`

## 수락 기준 (Acceptance Criteria)

### 빌드 (자동 검증)
- [ ] `./gradlew compileJava` 성공
- [ ] 컴파일 경고 없음 (domain 코드 기준)

### 아키텍처
- [ ] 패키지 구조가 CLAUDE.md 명세와 일치
- [ ] BaseEntity에 id, createdAt, updatedAt, deletedAt 필드 존재
- [ ] ApiResponse<T>에 success(), error() 정적 팩토리 메서드 존재
- [ ] PageResponse<T>에 Spring Page 변환 메서드 존재
- [ ] GlobalExceptionHandler가 BusinessException을 처리
- [ ] ErrorCode가 enum으로 정의

### 보안
- [ ] SecurityConfig에서 MVP 단계 기본 설정 (CORS, CSRF 비활성화, 세션 STATELESS)
- [ ] JwtTokenProvider에 토큰 생성/검증 메서드 골격 존재
- [ ] JwtAuthenticationFilter가 SecurityFilterChain에 등록

### 인프라
- [ ] docker-compose.yml에 PostgreSQL 15 + Redis 7 정의
- [ ] application.yml에 DB/Redis 연결 설정
- [ ] Flyway 마이그레이션 경로 설정

### DB
- [ ] V1__init_auth_tables.sql에 role, permission, role_permission, member 테이블 정의
- [ ] RBAC 스펙 준수 (Role-Permission N:M 관계)

### 코드 품질
- [ ] Java 21 record 활용 (ErrorResponse, JwtProperties 등)
- [ ] Lombok 최소 사용 (@Getter, @RequiredArgsConstructor 정도)

## 예상 파일 수: ~30개
## 의존성: 없음 (첫 스프린트)
