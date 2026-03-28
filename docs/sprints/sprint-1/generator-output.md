# Sprint 1 Generator Output

## Created Files

| File Path | Type | Description |
|-----------|------|-------------|
| domain/admin/entity/Role.java | Entity | V1 role 테이블 매핑, ManyToMany Permission |
| domain/admin/entity/Permission.java | Entity | V1 permission 테이블 매핑 |
| domain/admin/entity/Member.java | Entity | V1 member 테이블 매핑, BaseEntity 상속 |
| domain/admin/entity/MemberConsent.java | Entity | 약관 동의 이력 |
| domain/admin/entity/MagicLink.java | Entity | 매직 링크 토큰 (UUID, 2FA, 10분 만료) |
| domain/admin/entity/RefreshToken.java | Entity | 리프레시 토큰 |
| domain/admin/entity/AgencyProfile.java | Entity | 에이전시/병원 프로필 |
| domain/staff/entity/StaffProfile.java | Entity | 실무자 프로필 (JSONB languages/vehicleInfo) |
| domain/admin/enums/MagicLinkTargetType.java | Enum | EMAIL, SMS |
| domain/staff/enums/StaffType.java | Enum | DRIVER, INTERPRETER |
| V2__auth_entities.sql | Migration | 5개 테이블 추가 |
| domain/admin/repository/*.java (6개) | Repository | JPA Repositories |
| domain/staff/repository/StaffProfileRepository.java | Repository | StaffProfile Repository |
| domain/admin/dto/*.java (18개) | DTO | Java 21 record DTOs |
| domain/admin/service/AuthService.java | Service | OAuth, MagicLink, Token, Consent, RBAC |
| domain/admin/service/MemberService.java | Service | Staff/Agency 프로필, 라이선스 검증 |
| domain/admin/controller/AuthController.java | Controller | 9개 Auth 엔드포인트 |
| domain/admin/controller/MemberController.java | Controller | 5개 Member 엔드포인트 |
| test/.../AuthServiceTest.java | Test | 10개 테스트 |
| test/.../MemberServiceTest.java | Test | 8개 테스트 |
| test/.../AuthControllerTest.java | Test | 6개 테스트 |
| test/.../MemberControllerTest.java | Test | 5개 테스트 |
| global/exception/ErrorCode.java | Modified | Auth/Staff/Agency 에러코드 15개 추가 |

## Decisions Made
- OAuth idToken 검증: MVP에서 간소화 (TODO 주석으로 실제 Google/Apple 검증 자리 표시)
- 매직 링크 발송: MVP에서 토큰 생성만, 실제 이메일/SMS 발송은 TODO
- Role/Permission: V1 테이블 매핑, Role 내 @ManyToMany로 Permission 관리 (별도 RolePermission Entity 불필요)
- StaffProfile: languages/vehicleInfo JSONB 사용으로 유연한 구조

## Ready for Evaluation: YES
