# Sprint 1 Contract: Auth + Member + RBAC

## 범위

### 요구사항
- PAT-101: 글로벌 소셜 로그인 (OAuth2 - Google, Apple)
- PAT-102: 글로벌 규제 동의 (T&C, Privacy)
- PAT-103: 비회원 매직 링크 (Guest View)
- STA-101: 간편 로그인 (Magic Link/SNS)
- STA-102: 실무자 프로필 관리
- ADM-801: RBAC 권한 관리
- ADM-302: 에이전시/병원 프로필 관리
- PAT-301: 에이전시/병원 라이선스 검증
- PAT-303: 배정된 실무자 프로필 조회

### 구현할 엔드포인트

#### Auth API (9개)
- `POST /api/v1/auth/oauth/{provider}` — OAuth2 소셜 로그인
- `POST /api/v1/auth/magic-link` — 매직 링크 발급
- `POST /api/v1/auth/magic-link/verify` — 매직 링크 인증 + 2FA
- `POST /api/v1/auth/refresh` — 토큰 갱신
- `POST /api/v1/auth/logout` — 로그아웃
- `POST /api/v1/auth/consent` — 약관 동의
- `GET /api/v1/auth/consent` — 약관 동의 내역 조회
- `GET /api/v1/auth/roles` — 역할 목록 조회 (ADMIN)
- `PUT /api/v1/auth/users/{userId}/role` — 사용자 역할 변경 (ADMIN)

#### Member API (5개)
- `GET /api/v1/members/staff/me` — 실무자 본인 프로필 조회
- `PUT /api/v1/members/staff/me` — 실무자 본인 프로필 수정
- `GET /api/v1/members/agency` — 에이전시 프로필 조회
- `PUT /api/v1/members/agency` — 에이전시 프로필 수정 (ADMIN)
- `GET /api/v1/members/agency/license` — 에이전시 라이선스 검증

### 생성할 Entity
- Member (Sprint 0 migration에서 테이블 생성됨, Entity 클래스 구현)
- Role (Entity 클래스)
- Permission (Entity 클래스)
- RolePermission (Entity 클래스)
- MemberConsent (약관 동의 이력)
- MagicLink (매직 링크 토큰)
- RefreshToken (리프레시 토큰)
- StaffProfile (실무자 상세 프로필)
- AgencyProfile (에이전시/병원 프로필)

### 생성할 파일 목록
- `src/main/java/com/k/medtour/domain/admin/entity/` — Role, Permission, RolePermission
- `src/main/java/com/k/medtour/domain/admin/entity/Member.java`
- `src/main/java/com/k/medtour/domain/admin/entity/MemberConsent.java`
- `src/main/java/com/k/medtour/domain/admin/entity/MagicLink.java`
- `src/main/java/com/k/medtour/domain/admin/entity/RefreshToken.java`
- `src/main/java/com/k/medtour/domain/admin/repository/` — 각 Entity Repository
- `src/main/java/com/k/medtour/domain/admin/dto/` — Request/Response DTOs
- `src/main/java/com/k/medtour/domain/admin/service/AuthService.java`
- `src/main/java/com/k/medtour/domain/admin/service/MemberService.java`
- `src/main/java/com/k/medtour/domain/admin/controller/AuthController.java`
- `src/main/java/com/k/medtour/domain/admin/controller/MemberController.java`
- `src/main/java/com/k/medtour/domain/staff/entity/StaffProfile.java`
- `src/main/java/com/k/medtour/domain/staff/repository/StaffProfileRepository.java`
- `src/main/java/com/k/medtour/domain/admin/entity/AgencyProfile.java`
- `src/main/resources/db/migration/V2__auth_entities.sql`
- `src/test/java/.../service/AuthServiceTest.java`
- `src/test/java/.../service/MemberServiceTest.java`
- `src/test/java/.../controller/AuthControllerTest.java`
- `src/test/java/.../controller/MemberControllerTest.java`

## 수락 기준 (Acceptance Criteria)

### 빌드 (자동 검증)
- [ ] `./gradlew compileJava` 성공
- [ ] `./gradlew test` 전체 통과
- [ ] 컴파일 경고 없음

### 테스트
- [ ] AuthService 단위 테스트 존재 (OAuth 로그인, 매직 링크, 토큰 갱신)
- [ ] MemberService 단위 테스트 존재 (프로필 조회/수정)
- [ ] AuthController @WebMvcTest 슬라이스 테스트 존재
- [ ] MemberController @WebMvcTest 슬라이스 테스트 존재
- [ ] @DisplayName 한국어 작성
- [ ] public 메서드당 최소 성공 1건 + 실패 1건

### 아키텍처
- [ ] Controller → Service → Repository 계층 준수
- [ ] DTO ↔ Entity 변환은 record의 static from() 메서드
- [ ] 모든 엔드포인트 ApiResponse<T> 래핑
- [ ] @PreAuthorize 어노테이션 (roles, consent 엔드포인트)
- [ ] Flyway V2 마이그레이션 존재

### 보안
- [ ] OAuth 토큰 검증 로직 (Google/Apple idToken 파싱)
- [ ] 매직 링크 2FA (생년월일 검증)
- [ ] 매직 링크 만료 처리 (10분)
- [ ] Refresh Token HttpOnly Cookie 설정
- [ ] 자기 자신 역할 변경 불가

### 코드 품질
- [ ] DTO는 Java 21 record 사용
- [ ] 커스텀 예외 BusinessException 상속
- [ ] Service 레이어 로깅

### DB
- [ ] Entity는 BaseEntity 상속 (MagicLink, RefreshToken, MemberConsent, StaffProfile, AgencyProfile)
- [ ] Role, Permission은 V1 마이그레이션 테이블 매핑
- [ ] FetchType.LAZY
- [ ] V2 마이그레이션에 MagicLink, RefreshToken, MemberConsent, StaffProfile, AgencyProfile 테이블

## 예상 파일 수: ~35개
## 의존성: Sprint 0 완료 필요
