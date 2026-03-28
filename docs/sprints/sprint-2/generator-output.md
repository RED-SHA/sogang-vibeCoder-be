# Sprint 2 Generator Output

## Created Files (22개)

| Category | Count | Files |
|----------|-------|-------|
| Enums | 5 | Gender, BloodType, PassportInputType, VerificationStatus, QuestionnaireStatus |
| Entities | 3 | PatientPassport, MedicalQuestionnaire, EmergencyContact |
| Migration | 1 | V3__patient_entities.sql |
| Repositories | 3 | PatientPassport, MedicalQuestionnaire, EmergencyContact |
| DTOs | 7 | Passport, Questionnaire, EmergencyContact (req/res) + PatientListResponse |
| Service | 1 | PatientService |
| Controller | 1 | PatientController (11 endpoints) |
| Tests | 2 | PatientServiceTest (16), PatientControllerTest (11) |

## Decisions Made
- 문진표 상세(allergies, medications, surgeries, conditions)는 JSONB 컬럼으로 유연한 구조
- 여권번호 마스킹: PassportResponse.from()에서 뒤 4자리 "****" 처리
- 본인 데이터 접근: Service에서 SecurityContext → UserPrincipal → memberId 비교, ADMIN/MASTER 전체 접근

## Ready for Evaluation: YES
