# 빌드 · 실행 가이드

## 환경 요구사항

- Java 11 이상 (`java`, `javac`, `jar` 명령어 사용 가능)
- 외부 의존성 없음 (Pure Java SE, JDK 내장 HttpServer 사용)
- 작업 디렉토리: 프로젝트 루트 (`Output_L3+L4/`)

---

## 스크립트로 한 번에 실행 (권장)

```bash
./build.sh
```

컴파일 → JAR 빌드 → 서버 실행까지 자동 처리됩니다.  
`out/` 디렉토리는 종료 시 자동 삭제되고, `kmedical.jar`는 루트에 유지됩니다.

---

## 단계별 실행

### 1단계 — 컴파일

```bash
mkdir -p out
find src -name "*.java" | xargs javac -d out -cp src/main/java
```

### 2단계 — JAR 빌드

```bash
jar cfe kmedical.jar com.kmedical.App -C out .
```

### 3단계 — 서버 실행

```bash
java -jar kmedical.jar
# 또는 클래스패스 직접 실행
java -cp out com.kmedical.App
```

서버 기동 후 출력 예시:

```
================================================
  K-의료관광솔루션 서버 기동
  http://localhost:8080
================================================
  GET  /api/health
  POST /api/quotations/requests     UC-P07 견적 요청
  ...
================================================
  종료: Ctrl+C
```

**종료:** `Ctrl+C` — ShutdownHook이 서버를 정상 종료합니다.

---

## API 엔드포인트 전체 목록

### Health
| Method | Path | 설명 |
|--------|------|------|
| GET | `/api/health` | 시스템 상태 확인 |

### Auth (SRV-C01)
| Method | Path | 설명 |
|--------|------|------|
| POST | `/api/auth/login` | UC-A01 OAuth 로그인 |
| POST | `/api/auth/logout` | 세션 무효화 |
| POST | `/api/auth/startup` | UC-O01 시스템 가동 |
| POST | `/api/auth/closedown` | UC-O02 시스템 종료 |

### AccessLink (SRV-C02)
| Method | Path | 설명 |
|--------|------|------|
| POST | `/api/access-links` | 접속 링크 생성 |
| POST | `/api/access-links/verify` | 매직링크 + 생년월일 검증 |
| DELETE | `/api/access-links/{token}` | 링크 무효화 |

### Passport (SRV-C03)
| Method | Path | 설명 |
|--------|------|------|
| POST | `/api/passports/upload` | UC-P03 여권 업로드 + OCR |
| POST | `/api/passports/review` | UC-A03 관리자 검토 |
| GET | `/api/passports/{id}` | 여권 정보 조회 |

### Patient (SRV-C04)
| Method | Path | 설명 |
|--------|------|------|
| GET | `/api/patients/{id}` | 환자 조회 |
| POST | `/api/patients/{id}/onboarding/submit` | UC-P04 온보딩 제출 |
| POST | `/api/patients/{id}/onboarding/approve` | 관리자 온보딩 승인 |
| POST | `/api/patients/{id}/questionnaire` | 문진표 저장 |
| GET | `/api/patients/{id}/questionnaire` | 문진표 조회 |
| POST | `/api/patients/{id}/emergency-contacts` | UC-P05 긴급연락처 추가 |
| GET | `/api/patients/{id}/emergency-contacts` | 긴급연락처 목록 |
| DELETE | `/api/patients/{id}/emergency-contacts/{cid}` | 긴급연락처 삭제 |

### Agency (SRV-C05)
| Method | Path | 설명 |
|--------|------|------|
| GET | `/api/agencies/{id}` | 에이전시 조회 |
| POST | `/api/agencies` | 에이전시 생성/수정 |
| PUT | `/api/agencies/{id}/verify` | 라이선스 인증 상태 변경 |

### Quotation (SRV-C06)
| Method | Path | 설명 |
|--------|------|------|
| POST | `/api/quotations/requests` | UC-P07 환자 견적 요청 |
| GET | `/api/quotations/requests/{id}` | 견적 요청 단건 조회 |
| POST | `/api/quotations/issue` | UC-A04 관리자 견적 발송 |
| POST | `/api/quotations/accept` | UC-P08 환자 견적 수락 |
| GET | `/api/quotations?requestId={id}` | 견적 목록 조회 |

### Template (SRV-C07)
| Method | Path | 설명 |
|--------|------|------|
| POST | `/api/templates` | 여정 템플릿 생성 |
| PUT | `/api/templates` | 여정 템플릿 수정 |
| GET | `/api/templates/{id}` | 템플릿 단건 조회 |
| GET | `/api/templates?agencyId={id}` | 에이전시 템플릿 목록 |
| POST | `/api/templates/{id}/items` | 템플릿 항목 추가 |
| GET | `/api/templates/{id}/items` | 템플릿 항목 목록 |

### Journey (SRV-C08)
| Method | Path | 설명 |
|--------|------|------|
| POST | `/api/journeys` | 여정 생성 |
| GET | `/api/journeys/{id}` | 여정 단건 조회 |
| POST | `/api/journeys/{id}/schedule` | UC-A06 일정 항목 추가 |
| GET | `/api/journeys/{id}/schedule` | 일정 목록 조회 |

### Staff (SRV-C09)
| Method | Path | 설명 |
|--------|------|------|
| GET | `/api/staff/{id}` | 스태프 프로필 조회 |
| PUT | `/api/staff/{id}/profile` | 프로필 수정 |
| PUT | `/api/staff/{id}/availability` | 가용 상태 변경 |
| GET | `/api/staff?agencyId={id}` | 에이전시 소속 스태프 목록 |

### StaffAssignment (SRV-C10)
| Method | Path | 설명 |
|--------|------|------|
| POST | `/api/assignments` | UC-A07 스태프 배정 |
| GET | `/api/assignments?scheduleItemId={id}` | 일정 항목별 배정 목록 |
| GET | `/api/assignments?staffId={id}` | 스태프별 배정 목록 |

### Work (SRV-C11)
| Method | Path | 설명 |
|--------|------|------|
| POST | `/api/work/status` | UC-S05 업무 상태 변경 기록 |
| POST | `/api/work/photos` | UC-S06 증빙 사진 업로드 |
| GET | `/api/work/history?assignmentId={id}` | 상태 변경 이력 조회 |

### Chat (SRV-C12)
| Method | Path | 설명 |
|--------|------|------|
| POST | `/api/chat/conversations` | 대화방 생성 |
| POST | `/api/chat/messages` | 메시지 발송 (자동 번역) |
| GET | `/api/chat/messages?conversationId={id}` | 메시지 목록 조회 |
| POST | `/api/chat/close` | 대화방 종료 |

### Alert (SRV-C13)
| Method | Path | 설명 |
|--------|------|------|
| POST | `/api/alerts` | UC-N01 알림 생성·발송 |
| GET | `/api/alerts?userId={id}` | 사용자 알림 이력 조회 |

### SOS (SRV-C14)
| Method | Path | 설명 |
|--------|------|------|
| POST | `/api/sos` | UC-S10 긴급 호출 생성 |
| PUT | `/api/sos/{id}/resolve` | UC-E02 긴급 호출 해결 |
| GET | `/api/sos/unresolved` | 미해결 긴급 호출 목록 |

### Invoice (SRV-C15)
| Method | Path | 설명 |
|--------|------|------|
| POST | `/api/invoices` | 인보이스 DRAFT 생성 |
| POST | `/api/invoices/{id}/issue` | UC-A11 인보이스 발행 |
| POST | `/api/invoices/{id}/cancel` | 인보이스 취소 |
| GET | `/api/invoices/{id}` | 인보이스 단건 조회 |
| GET | `/api/invoices?journeyId={id}` | 여정별 인보이스 목록 |

### Guide (SRV-C16)
| Method | Path | 설명 |
|--------|------|------|
| POST | `/api/guides` | UC-A10 회복 가이드 등록 |
| POST | `/api/guides/deliver` | 환자에게 가이드 배포 |
| GET | `/api/guides/{id}` | 가이드 단건 조회 |
| GET | `/api/guides?patientId={id}` | 환자 배포 가이드 목록 |

### Report (SRV-C17)
| Method | Path | 설명 |
|--------|------|------|
| POST | `/api/reports` | UC-S11 일일 업무 리포트 제출 |
| GET | `/api/reports?staffId={id}` | 스태프 리포트 목록 |

### RBAC (SRV-C18)
| Method | Path | 설명 |
|--------|------|------|
| POST | `/api/rbac/assign` | UC-A13 역할 부여 |
| POST | `/api/rbac/revoke` | 역할 회수 |
| GET | `/api/rbac/{userId}/roles` | 현재 역할 조회 |
| GET | `/api/rbac/{userId}/history` | 역할 변경 이력 조회 |

### Dashboard (SRV-C19)
| Method | Path | 설명 |
|--------|------|------|
| GET | `/api/dashboard?agencyId={id}&adminUserId={id}` | UC-A02 관리자 대시보드 집계 |

---

## 요청/응답 예시

### GET /api/health

```bash
curl http://localhost:8080/api/health
```

```json
{
  "status": "UP",
  "systemState": "RUNNING",
  "timestamp": "2026-05-24T00:00:00"
}
```

### POST /api/quotations/requests

```bash
curl -X POST http://localhost:8080/api/quotations/requests \
  -H "Content-Type: application/json" \
  -d '{
    "patientId": "p-001",
    "desiredVisitDate": "2026-08-15",
    "surgeryType": "RHINOPLASTY",
    "requiredService": "INTERPRETATION"
  }'
```

```json
{
  "quotationRequestId": "...",
  "patientId": "p-001",
  "surgeryType": "RHINOPLASTY",
  "status": "OPEN",
  "expiresAt": "...",
  "createdAt": "..."
}
```

### POST /api/quotations/issue

```bash
curl -X POST http://localhost:8080/api/quotations/issue \
  -H "Content-Type: application/json" \
  -d '{
    "quotationRequestId": "{rfqId}",
    "patientId": "p-001",
    "medicalFeeUSD": "3500.00",
    "conciergeFeeUSD": "500.00"
  }'
```

### POST /api/quotations/accept

```bash
curl -X POST http://localhost:8080/api/quotations/accept \
  -H "Content-Type: application/json" \
  -d '{"quotationId": "{quotationId}"}'
```

### POST /api/journeys

```bash
curl -X POST http://localhost:8080/api/journeys \
  -H "Content-Type: application/json" \
  -d '{
    "patientId": "p-001",
    "agencyId": "agency-001",
    "quotationId": "{quotationId}",
    "arrivalDate": "2026-08-14",
    "departureDate": "2026-08-20"
  }'
```

### POST /api/journeys/{id}/schedule

```bash
curl -X POST http://localhost:8080/api/journeys/{journeyId}/schedule \
  -H "Content-Type: application/json" \
  -d '{
    "title": "코 성형 수술",
    "itemType": "SURGERY",
    "locationAddressEn": "123 Gangnam-daero, Seoul",
    "isCritical": "true"
  }'
```

---

## 정리

```bash
rm -rf out kmedical.jar
```
