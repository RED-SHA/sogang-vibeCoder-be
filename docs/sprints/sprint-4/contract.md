# Sprint 4 Contract: Proposal/Quotation

## 범위

### 요구사항
- ADM-301: 스마트 견적서 생성
- PAT-401: 수술/컨시어지 견적 요청
- PAT-402: 견적서 수신 및 비교
- PAT-403: 견적 수락 및 기승인

### 구현할 엔드포인트 (8개)
- `POST /api/v1/proposals` — 견적서 생성 (ADMIN)
- `GET /api/v1/proposals` — 견적서 목록 조회 (ADMIN, 페이징+필터)
- `GET /api/v1/proposals/{proposalId}` — 견적서 상세 (ADMIN/PATIENT)
- `POST /api/v1/proposals/{proposalId}/send` — 견적서 발송 DRAFT→SENT
- `POST /api/v1/proposals/request` — 견적 요청 (PATIENT)
- `GET /api/v1/proposals/me` — 내 견적서 목록 (PATIENT)
- `POST /api/v1/proposals/{proposalId}/accept` — 견적 수락 SENT→ACCEPTED
- `POST /api/v1/proposals/{proposalId}/reject` — 견적 거절 SENT→REJECTED

### Entity
- Proposal (견적서 마스터)
- ProposalItem (견적 항목)
- ProposalRequest (환자 견적 요청)

## 수락 기준
- [ ] compileJava + compileTestJava 성공
- [ ] ProposalService 단위 테스트, ProposalController 슬라이스 테스트
- [ ] 상태 전이 검증 (DRAFT→SENT→ACCEPTED/REJECTED)
- [ ] 금액 계산 (subtotal, discount, total)
- [ ] 본인 견적서만 접근 (PATIENT)
- [ ] Flyway V5
- [ ] Java 21 record DTO

## 예상 파일 수: ~20개
## 의존성: Sprint 2 완료
