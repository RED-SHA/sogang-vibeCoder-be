# K-의료 관광 솔루션 REST API 명세서

> **Version**: 1.0.0
> **Last Updated**: 2026-03-21
> **Base URL**: `https://{domain}/api/v1`

---

## 목차

1. [공통 스펙](#1-공통-스펙)
2. [Auth API](#2-auth-api)
3. [Member API](#3-member-api)
4. [Patient API](#4-patient-api)
5. [Staff API](#5-staff-api)
6. [Proposal API](#6-proposal-api)
7. [Journey API](#7-journey-api)
8. [Chat API](#8-chat-api)
9. [Notification API](#9-notification-api)
10. [Aftercare API](#10-aftercare-api)
11. [Dashboard API](#11-dashboard-api)
12. [File API](#12-file-api)
13. [Profile API](#13-profile-api)
14. [WebSocket 이벤트 명세](#14-websocket-이벤트-명세)
15. [요구사항-API 매핑 테이블](#15-요구사항-api-매핑-테이블)

---

## 1. 공통 스펙

### 1.1 공통 응답 포맷

**성공 응답**:
```json
{
  "success": true,
  "message": "string",
  "data": { }
}
```

**에러 응답**:
```json
{
  "success": false,
  "message": "에러 메시지",
  "errorCode": "AUTH_001",
  "data": null
}
```

**페이지네이션 응답**:
```json
{
  "success": true,
  "message": "조회 성공",
  "data": {
    "content": [],
    "page": 0,
    "size": 20,
    "totalElements": 100,
    "totalPages": 5
  }
}
```

### 1.2 공통 HTTP 상태 코드

| 코드 | 의미 |
|------|------|
| 200 | 성공 |
| 201 | 생성 성공 |
| 204 | 삭제 성공 (No Content) |
| 400 | 잘못된 요청 (유효성 검증 실패) |
| 401 | 인증 실패 (토큰 없음/만료) |
| 403 | 권한 없음 |
| 404 | 리소스 없음 |
| 409 | 충돌 (중복 등) |
| 500 | 서버 내부 오류 |

### 1.3 인증 방식

| 방식 | 헤더/방법 | 설명 |
|------|----------|------|
| Bearer JWT | `Authorization: Bearer {accessToken}` | 일반 인증 (관리자, 환자, 실무자) |
| Magic Link | URL 쿼리 파라미터 `?token={magicToken}` + 2차 인증 | 비회원/실무자 간편 접근 |
| Refresh Token | HttpOnly Cookie `refreshToken` | Access Token 갱신용 |

### 1.4 날짜/시간 형식

- ISO 8601: `2026-03-21T14:30:00+09:00`
- 서버 저장: UTC
- 응답: 클라이언트 요청 타임존(`Accept-Timezone` 헤더) 기준 변환

### 1.5 다국어 지원

- 요청 헤더: `Accept-Language: en | zh | ja | ar | ko`
- 서버 응답 메시지는 해당 언어로 반환
- 미지원 언어 시 기본값: `en`

### 1.6 공통 Request Headers

| 헤더 | 필수 | 설명 |
|------|------|------|
| `Authorization` | 조건부 | Bearer JWT 토큰 |
| `Content-Type` | POST/PUT/PATCH | `application/json` |
| `Accept-Language` | 아니오 | 응답 언어 (기본: en) |
| `Accept-Timezone` | 아니오 | 응답 타임존 (기본: UTC) |

---

## 2. Auth API

### 2.1 소셜 로그인 (OAuth2)

> 관련 요구사항: PAT-101

#### `POST /api/v1/auth/oauth/{provider}`

**설명**: Google/Apple OAuth2 소셜 로그인 처리

**인증**: 불필요
**권한**: PUBLIC

**Path Parameters**:
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| provider | String | 예 | OAuth 제공자 (`google`, `apple`) |

**Request Body**:
```json
{
  "idToken": "eyJhbGciOiJSUzI1NiIs...",
  "deviceInfo": {
    "platform": "WEB",
    "language": "en"
  }
}
```

**Response** (`200 OK`):
```json
{
  "success": true,
  "message": "로그인 성공",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "refreshToken": "Set-Cookie (HttpOnly)",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "user": {
      "id": 1,
      "email": "patient@example.com",
      "name": "John Doe",
      "role": "ROLE_PATIENT",
      "isNewUser": false
    }
  }
}
```

**Error Cases**:
| HTTP Status | Error Code | 설명 |
|-------------|-----------|------|
| 400 | AUTH_001 | 유효하지 않은 OAuth 토큰 |
| 400 | AUTH_002 | 지원하지 않는 OAuth 제공자 |
| 409 | AUTH_003 | 이미 다른 제공자로 가입된 이메일 |

---

### 2.2 매직 링크 발급

> 관련 요구사항: PAT-103, STA-101

#### `POST /api/v1/auth/magic-link`

**설명**: 비회원(환자) 또는 실무자에게 매직 링크를 발급하여 이메일/SMS로 전송

**인증**: 불필요
**권한**: PUBLIC

**Request Body**:
```json
{
  "target": "patient@example.com",
  "targetType": "EMAIL",
  "role": "ROLE_PATIENT",
  "language": "en"
}
```

**Response** (`200 OK`):
```json
{
  "success": true,
  "message": "매직 링크가 전송되었습니다",
  "data": {
    "expiresIn": 600,
    "targetMasked": "pat***@example.com"
  }
}
```

**Error Cases**:
| HTTP Status | Error Code | 설명 |
|-------------|-----------|------|
| 400 | AUTH_010 | 유효하지 않은 이메일/전화번호 형식 |
| 429 | AUTH_011 | 매직 링크 발급 횟수 초과 (분당 3회) |

---

### 2.3 매직 링크 인증

> 관련 요구사항: PAT-103, STA-101

#### `POST /api/v1/auth/magic-link/verify`

**설명**: 매직 링크 토큰 + 2차 인증(생년월일)으로 로그인 처리

**인증**: 불필요
**권한**: PUBLIC

**Request Body**:
```json
{
  "token": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "birthDate": "1990-05-15"
}
```

**Response** (`200 OK`):
```json
{
  "success": true,
  "message": "인증 성공",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "user": {
      "id": 5,
      "name": "Guest Patient",
      "role": "ROLE_PATIENT"
    }
  }
}
```

**Error Cases**:
| HTTP Status | Error Code | 설명 |
|-------------|-----------|------|
| 400 | AUTH_020 | 만료된 매직 링크 |
| 401 | AUTH_021 | 2차 인증(생년월일) 불일치 |
| 404 | AUTH_022 | 존재하지 않는 매직 링크 토큰 |

---

### 2.4 토큰 갱신

> 관련 요구사항: PAT-101, STA-101

#### `POST /api/v1/auth/refresh`

**설명**: Refresh Token으로 Access Token 갱신

**인증**: 불필요 (Refresh Token은 HttpOnly Cookie로 전송)
**권한**: PUBLIC

**Request Body**: 없음 (Cookie에서 Refresh Token 추출)

**Response** (`200 OK`):
```json
{
  "success": true,
  "message": "토큰 갱신 성공",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "tokenType": "Bearer",
    "expiresIn": 3600
  }
}
```

**Error Cases**:
| HTTP Status | Error Code | 설명 |
|-------------|-----------|------|
| 401 | AUTH_030 | 만료된 Refresh Token |
| 401 | AUTH_031 | 유효하지 않은 Refresh Token |

---

### 2.5 로그아웃

> 관련 요구사항: PAT-101, STA-101

#### `POST /api/v1/auth/logout`

**설명**: 현재 세션 로그아웃 (Refresh Token 무효화)

**인증**: Bearer JWT
**권한**: ROLE_ADMIN, ROLE_PATIENT, ROLE_STAFF

**Response** (`200 OK`):
```json
{
  "success": true,
  "message": "로그아웃 성공",
  "data": null
}
```

---

### 2.6 약관 동의

> 관련 요구사항: PAT-102

#### `POST /api/v1/auth/consent`

**설명**: 글로벌 규제 동의 (이용약관, 개인정보 처리방침, 의료정보 수집 동의)

**인증**: Bearer JWT
**권한**: ROLE_PATIENT

**Request Body**:
```json
{
  "termsOfService": true,
  "privacyPolicy": true,
  "medicalDataConsent": true,
  "marketingConsent": false,
  "consentVersion": "2026-03-01"
}
```

**Response** (`200 OK`):
```json
{
  "success": true,
  "message": "동의 처리 완료",
  "data": {
    "consentedAt": "2026-03-21T14:30:00+09:00"
  }
}
```

**Error Cases**:
| HTTP Status | Error Code | 설명 |
|-------------|-----------|------|
| 400 | AUTH_040 | 필수 동의 항목 미체크 |

---

### 2.7 약관 동의 내역 조회

> 관련 요구사항: PAT-102

#### `GET /api/v1/auth/consent`

**설명**: 현재 사용자의 약관 동의 내역 조회

**인증**: Bearer JWT
**권한**: ROLE_PATIENT

**Response** (`200 OK`):
```json
{
  "success": true,
  "message": "조회 성공",
  "data": {
    "termsOfService": true,
    "privacyPolicy": true,
    "medicalDataConsent": true,
    "marketingConsent": false,
    "consentVersion": "2026-03-01",
    "consentedAt": "2026-03-21T14:30:00+09:00"
  }
}
```

---

### 2.8 RBAC 권한 관리 - 역할 목록 조회

> 관련 요구사항: ADM-801

#### `GET /api/v1/auth/roles`

**설명**: 시스템에 정의된 역할 목록 조회

**인증**: Bearer JWT
**권한**: ROLE_ADMIN

**Response** (`200 OK`):
```json
{
  "success": true,
  "message": "조회 성공",
  "data": [
    {
      "id": 1,
      "name": "ROLE_ADMIN",
      "description": "시스템 관리자",
      "permissions": ["DASHBOARD_VIEW", "PATIENT_MANAGE", "STAFF_MANAGE", "JOURNEY_MANAGE", "CHAT_MONITOR", "PROPOSAL_MANAGE", "AFTERCARE_MANAGE", "RBAC_MANAGE"]
    },
    {
      "id": 2,
      "name": "ROLE_PATIENT",
      "description": "환자",
      "permissions": ["JOURNEY_VIEW", "CHAT_SEND", "PROPOSAL_VIEW", "AFTERCARE_VIEW"]
    },
    {
      "id": 3,
      "name": "ROLE_STAFF",
      "description": "실무자 (기사/통역사)",
      "permissions": ["JOURNEY_VIEW", "STATUS_UPDATE", "CHAT_SEND"]
    }
  ]
}
```

---

### 2.9 RBAC 권한 관리 - 사용자 역할 변경

> 관련 요구사항: ADM-801

#### `PUT /api/v1/auth/users/{userId}/role`

**설명**: 특정 사용자의 역할을 변경

**인증**: Bearer JWT
**권한**: ROLE_ADMIN

**Path Parameters**:
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| userId | Long | 예 | 대상 사용자 ID |

**Request Body**:
```json
{
  "roleId": 3
}
```

**Response** (`200 OK`):
```json
{
  "success": true,
  "message": "역할 변경 완료",
  "data": {
    "userId": 10,
    "previousRole": "ROLE_STAFF",
    "newRole": "ROLE_ADMIN"
  }
}
```

**Error Cases**:
| HTTP Status | Error Code | 설명 |
|-------------|-----------|------|
| 404 | AUTH_050 | 존재하지 않는 사용자 |
| 400 | AUTH_051 | 존재하지 않는 역할 ID |
| 403 | AUTH_052 | 자기 자신의 역할은 변경 불가 |

---

## 3. Member API

### 3.1 실무자 프로필 관리 - 조회

> 관련 요구사항: STA-102

#### `GET /api/v1/members/staff/me`

**설명**: 로그인한 실무자 본인의 프로필 조회

**인증**: Bearer JWT
**권한**: ROLE_STAFF

**Response** (`200 OK`):
```json
{
  "success": true,
  "message": "조회 성공",
  "data": {
    "id": 10,
    "name": "김기사",
    "email": "driver.kim@example.com",
    "phone": "+82-10-1234-5678",
    "role": "DRIVER",
    "staffType": "DRIVER",
    "languages": ["ko", "en"],
    "profileImageUrl": "https://cdn.example.com/profiles/10.jpg",
    "vehicleInfo": {
      "type": "SEDAN",
      "plateNumber": "서울 12가 3456"
    },
    "isAvailable": true,
    "createdAt": "2026-01-15T09:00:00Z"
  }
}
```

---

### 3.2 실무자 프로필 관리 - 수정

> 관련 요구사항: STA-102

#### `PUT /api/v1/members/staff/me`

**설명**: 로그인한 실무자 본인의 프로필 수정

**인증**: Bearer JWT
**권한**: ROLE_STAFF

**Request Body**:
```json
{
  "name": "김기사",
  "phone": "+82-10-1234-5678",
  "languages": ["ko", "en", "zh"],
  "profileImageUrl": "https://cdn.example.com/profiles/10.jpg",
  "vehicleInfo": {
    "type": "SUV",
    "plateNumber": "서울 12가 3456"
  }
}
```

**Response** (`200 OK`):
```json
{
  "success": true,
  "message": "프로필 수정 완료",
  "data": {
    "id": 10,
    "name": "김기사",
    "phone": "+82-10-1234-5678",
    "languages": ["ko", "en", "zh"],
    "profileImageUrl": "https://cdn.example.com/profiles/10.jpg",
    "vehicleInfo": {
      "type": "SUV",
      "plateNumber": "서울 12가 3456"
    },
    "updatedAt": "2026-03-21T15:00:00Z"
  }
}
```

**Error Cases**:
| HTTP Status | Error Code | 설명 |
|-------------|-----------|------|
| 400 | MEM_001 | 유효하지 않은 전화번호 형식 |
| 400 | MEM_002 | 지원하지 않는 언어 코드 |

---

### 3.3 에이전시/병원 프로필 관리 - 목록 조회

> 관련 요구사항: ADM-302

#### `GET /api/v1/members/organizations`

**설명**: 등록된 에이전시/병원 프로필 목록 조회

**인증**: Bearer JWT
**권한**: ROLE_ADMIN

**Query Parameters**:
| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|------|--------|------|
| type | String | 아니오 | 전체 | `AGENCY`, `HOSPITAL` |
| page | int | 아니오 | 0 | 페이지 번호 |
| size | int | 아니오 | 20 | 페이지 크기 |

**Response** (`200 OK`):
```json
{
  "success": true,
  "message": "조회 성공",
  "data": {
    "content": [
      {
        "id": 1,
        "type": "HOSPITAL",
        "name": "서울 뷰티 클리닉",
        "licenseNumber": "H-2025-001234",
        "licenseVerified": true,
        "address": "서울시 강남구 ...",
        "contactEmail": "info@beautyclinic.kr",
        "contactPhone": "+82-2-1234-5678",
        "specialties": ["성형외과", "피부과"],
        "logoUrl": "https://cdn.example.com/orgs/1.png"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 15,
    "totalPages": 1
  }
}
```

---

### 3.4 에이전시/병원 프로필 관리 - 생성

> 관련 요구사항: ADM-302

#### `POST /api/v1/members/organizations`

**설명**: 새 에이전시/병원 프로필 등록

**인증**: Bearer JWT
**권한**: ROLE_ADMIN

**Request Body**:
```json
{
  "type": "HOSPITAL",
  "name": "서울 뷰티 클리닉",
  "licenseNumber": "H-2025-001234",
  "address": "서울시 강남구 ...",
  "contactEmail": "info@beautyclinic.kr",
  "contactPhone": "+82-2-1234-5678",
  "specialties": ["성형외과", "피부과"],
  "logoUrl": "https://cdn.example.com/orgs/1.png"
}
```

**Response** (`201 Created`):
```json
{
  "success": true,
  "message": "등록 완료",
  "data": {
    "id": 2,
    "type": "HOSPITAL",
    "name": "서울 뷰티 클리닉",
    "licenseVerified": false,
    "createdAt": "2026-03-21T15:00:00Z"
  }
}
```

**Error Cases**:
| HTTP Status | Error Code | 설명 |
|-------------|-----------|------|
| 400 | MEM_010 | 필수 항목 누락 |
| 409 | MEM_011 | 이미 등록된 라이선스 번호 |

---

### 3.5 에이전시/병원 프로필 관리 - 수정

> 관련 요구사항: ADM-302

#### `PUT /api/v1/members/organizations/{organizationId}`

**설명**: 에이전시/병원 프로필 정보 수정

**인증**: Bearer JWT
**권한**: ROLE_ADMIN

**Path Parameters**:
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| organizationId | Long | 예 | 조직 ID |

**Request Body**:
```json
{
  "name": "서울 뷰티 클리닉 (강남점)",
  "address": "서울시 강남구 ...",
  "contactEmail": "gangnam@beautyclinic.kr",
  "specialties": ["성형외과", "피부과", "안과"]
}
```

**Response** (`200 OK`):
```json
{
  "success": true,
  "message": "수정 완료",
  "data": {
    "id": 1,
    "name": "서울 뷰티 클리닉 (강남점)",
    "updatedAt": "2026-03-21T16:00:00Z"
  }
}
```

**Error Cases**:
| HTTP Status | Error Code | 설명 |
|-------------|-----------|------|
| 404 | MEM_020 | 존재하지 않는 조직 |

---

## 4. Patient API

### 4.1 여권 정보 업로드

> 관련 요구사항: PAT-201, ADM-201

#### `POST /api/v1/patients/me/passport`

**설명**: 환자 여권 정보 업로드 (수동 입력 또는 OCR 이미지)

**인증**: Bearer JWT
**권한**: ROLE_PATIENT

**Request Body**:
```json
{
  "inputType": "OCR",
  "fileId": 123,
  "manual": {
    "passportNumber": "M12345678",
    "fullName": "JOHN DOE",
    "nationality": "US",
    "birthDate": "1990-05-15",
    "expiryDate": "2030-12-31",
    "gender": "MALE"
  }
}
```

> `inputType`이 `OCR`이면 `fileId` 필수, `MANUAL`이면 `manual` 객체 필수

**Response** (`201 Created`):
```json
{
  "success": true,
  "message": "여권 정보 등록 완료",
  "data": {
    "id": 1,
    "passportNumber": "M1234****",
    "fullName": "JOHN DOE",
    "nationality": "US",
    "birthDate": "1990-05-15",
    "expiryDate": "2030-12-31",
    "gender": "MALE",
    "ocrConfidence": 0.95,
    "verificationStatus": "PENDING",
    "createdAt": "2026-03-21T14:30:00Z"
  }
}
```

**Error Cases**:
| HTTP Status | Error Code | 설명 |
|-------------|-----------|------|
| 400 | PAT_001 | OCR 인식 실패 (이미지 품질 부족) |
| 400 | PAT_002 | 필수 여권 정보 누락 |
| 409 | PAT_003 | 이미 여권 정보가 등록됨 |

---

### 4.2 여권 정보 조회

> 관련 요구사항: PAT-201, ADM-201

#### `GET /api/v1/patients/{patientId}/passport`

**설명**: 환자 여권 정보 조회 (환자 본인 또는 관리자)

**인증**: Bearer JWT
**권한**: ROLE_PATIENT (본인만), ROLE_ADMIN

**Path Parameters**:
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| patientId | Long | 예 | 환자 ID |

**Response** (`200 OK`):
```json
{
  "success": true,
  "message": "조회 성공",
  "data": {
    "id": 1,
    "passportNumber": "M1234****",
    "fullName": "JOHN DOE",
    "nationality": "US",
    "birthDate": "1990-05-15",
    "expiryDate": "2030-12-31",
    "gender": "MALE",
    "verificationStatus": "VERIFIED",
    "verifiedAt": "2026-03-21T15:00:00Z"
  }
}
```

**Error Cases**:
| HTTP Status | Error Code | 설명 |
|-------------|-----------|------|
| 403 | PAT_010 | 타인의 여권 정보 접근 시도 |
| 404 | PAT_011 | 등록된 여권 정보 없음 |

---

### 4.3 의료/알레르기 문진표 제출

> 관련 요구사항: PAT-202

#### `POST /api/v1/patients/me/medical-questionnaire`

**설명**: 영문 의료 및 알레르기 문진표 제출

**인증**: Bearer JWT
**권한**: ROLE_PATIENT

**Request Body**:
```json
{
  "bloodType": "A_POSITIVE",
  "height": 175.5,
  "weight": 70.0,
  "allergies": [
    {
      "type": "DRUG",
      "name": "Penicillin",
      "severity": "SEVERE"
    }
  ],
  "currentMedications": [
    {
      "name": "Aspirin",
      "dosage": "100mg",
      "frequency": "DAILY"
    }
  ],
  "pastSurgeries": [
    {
      "name": "Appendectomy",
      "date": "2020-06-15",
      "hospital": "NYC Hospital"
    }
  ],
  "chronicConditions": ["HYPERTENSION"],
  "additionalNotes": "No special conditions"
}
```

**Response** (`201 Created`):
```json
{
  "success": true,
  "message": "문진표 제출 완료",
  "data": {
    "id": 1,
    "patientId": 5,
    "submittedAt": "2026-03-21T14:30:00Z",
    "status": "SUBMITTED"
  }
}
```

**Error Cases**:
| HTTP Status | Error Code | 설명 |
|-------------|-----------|------|
| 400 | PAT_020 | 필수 항목 누락 |
| 409 | PAT_021 | 이미 문진표가 제출됨 (수정은 PUT 사용) |

---

### 4.4 의료/알레르기 문진표 조회

> 관련 요구사항: PAT-202, ADM-201

#### `GET /api/v1/patients/{patientId}/medical-questionnaire`

**설명**: 환자 문진표 조회

**인증**: Bearer JWT
**권한**: ROLE_PATIENT (본인만), ROLE_ADMIN

**Path Parameters**:
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| patientId | Long | 예 | 환자 ID |

**Response** (`200 OK`):
```json
{
  "success": true,
  "message": "조회 성공",
  "data": {
    "id": 1,
    "bloodType": "A_POSITIVE",
    "height": 175.5,
    "weight": 70.0,
    "allergies": [
      {
        "type": "DRUG",
        "name": "Penicillin",
        "severity": "SEVERE"
      }
    ],
    "currentMedications": [],
    "pastSurgeries": [],
    "chronicConditions": ["HYPERTENSION"],
    "additionalNotes": "No special conditions",
    "submittedAt": "2026-03-21T14:30:00Z"
  }
}
```

---

### 4.5 의료/알레르기 문진표 수정

> 관련 요구사항: PAT-202

#### `PUT /api/v1/patients/me/medical-questionnaire`

**설명**: 제출된 문진표 수정

**인증**: Bearer JWT
**권한**: ROLE_PATIENT

**Request Body**: (4.3과 동일한 포맷)

**Response** (`200 OK`):
```json
{
  "success": true,
  "message": "문진표 수정 완료",
  "data": {
    "id": 1,
    "updatedAt": "2026-03-21T16:00:00Z"
  }
}
```

---

### 4.6 긴급 연락처 등록

> 관련 요구사항: PAT-203

#### `POST /api/v1/patients/me/emergency-contacts`

**설명**: 환자의 긴급 연락처 등록

**인증**: Bearer JWT
**권한**: ROLE_PATIENT

**Request Body**:
```json
{
  "name": "Jane Doe",
  "relationship": "SPOUSE",
  "phone": "+1-555-0100",
  "email": "jane.doe@example.com",
  "isPrimary": true
}
```

**Response** (`201 Created`):
```json
{
  "success": true,
  "message": "긴급 연락처 등록 완료",
  "data": {
    "id": 1,
    "name": "Jane Doe",
    "relationship": "SPOUSE",
    "phone": "+1-555-0100",
    "email": "jane.doe@example.com",
    "isPrimary": true,
    "createdAt": "2026-03-21T14:30:00Z"
  }
}
```

**Error Cases**:
| HTTP Status | Error Code | 설명 |
|-------------|-----------|------|
| 400 | PAT_030 | 유효하지 않은 전화번호 형식 |
| 409 | PAT_031 | 이미 등록된 기본 연락처가 있음 (isPrimary 충돌) |

---

### 4.7 긴급 연락처 목록 조회

> 관련 요구사항: PAT-203

#### `GET /api/v1/patients/me/emergency-contacts`

**설명**: 환자의 긴급 연락처 목록 조회

**인증**: Bearer JWT
**권한**: ROLE_PATIENT

**Response** (`200 OK`):
```json
{
  "success": true,
  "message": "조회 성공",
  "data": [
    {
      "id": 1,
      "name": "Jane Doe",
      "relationship": "SPOUSE",
      "phone": "+1-555-0100",
      "email": "jane.doe@example.com",
      "isPrimary": true
    }
  ]
}
```

---

### 4.8 긴급 연락처 수정

> 관련 요구사항: PAT-203

#### `PUT /api/v1/patients/me/emergency-contacts/{contactId}`

**설명**: 긴급 연락처 정보 수정

**인증**: Bearer JWT
**권한**: ROLE_PATIENT

**Path Parameters**:
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| contactId | Long | 예 | 연락처 ID |

**Request Body**:
```json
{
  "name": "Jane Doe",
  "relationship": "SPOUSE",
  "phone": "+1-555-0199",
  "email": "jane.doe@example.com",
  "isPrimary": true
}
```

**Response** (`200 OK`):
```json
{
  "success": true,
  "message": "수정 완료",
  "data": {
    "id": 1,
    "name": "Jane Doe",
    "updatedAt": "2026-03-21T16:00:00Z"
  }
}
```

---

### 4.9 긴급 연락처 삭제

> 관련 요구사항: PAT-203

#### `DELETE /api/v1/patients/me/emergency-contacts/{contactId}`

**설명**: 긴급 연락처 삭제

**인증**: Bearer JWT
**권한**: ROLE_PATIENT

**Path Parameters**:
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| contactId | Long | 예 | 연락처 ID |

**Response** (`204 No Content`)

---

### 4.10 환자 DB 목록 조회 (관리자)

> 관련 요구사항: ADM-201

#### `GET /api/v1/patients`

**설명**: 관리자용 환자 목록 조회 (서류 검토 포함)

**인증**: Bearer JWT
**권한**: ROLE_ADMIN

**Query Parameters**:
| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|------|--------|------|
| keyword | String | 아니오 | - | 이름/이메일 검색 |
| verificationStatus | String | 아니오 | 전체 | `PENDING`, `VERIFIED`, `REJECTED` |
| page | int | 아니오 | 0 | 페이지 번호 |
| size | int | 아니오 | 20 | 페이지 크기 |

**Response** (`200 OK`):
```json
{
  "success": true,
  "message": "조회 성공",
  "data": {
    "content": [
      {
        "id": 5,
        "name": "John Doe",
        "email": "patient@example.com",
        "nationality": "US",
        "phone": "+1-555-0100",
        "passportVerified": true,
        "questionnaireSubmitted": true,
        "emergencyContactRegistered": true,
        "consentCompleted": true,
        "createdAt": "2026-03-01T10:00:00Z"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 50,
    "totalPages": 3
  }
}
```

---

### 4.11 환자 서류 검토 상태 변경 (관리자)

> 관련 요구사항: ADM-201

#### `PATCH /api/v1/patients/{patientId}/verification`

**설명**: 환자 서류(여권, 문진표)의 검토 상태를 변경

**인증**: Bearer JWT
**권한**: ROLE_ADMIN

**Path Parameters**:
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| patientId | Long | 예 | 환자 ID |

**Request Body**:
```json
{
  "documentType": "PASSPORT",
  "status": "VERIFIED",
  "rejectionReason": null
}
```

**Response** (`200 OK`):
```json
{
  "success": true,
  "message": "검토 완료",
  "data": {
    "patientId": 5,
    "documentType": "PASSPORT",
    "status": "VERIFIED",
    "reviewedBy": 1,
    "reviewedAt": "2026-03-21T15:00:00Z"
  }
}
```

**Error Cases**:
| HTTP Status | Error Code | 설명 |
|-------------|-----------|------|
| 400 | PAT_040 | REJECTED 시 rejectionReason 필수 |
| 404 | PAT_041 | 해당 환자의 서류가 없음 |

---

## 5. Staff API

### 5.1 실무자 목록 조회 (관리자)

> 관련 요구사항: ADM-601

#### `GET /api/v1/staff`

**설명**: 등록된 실무자(기사/통역사) 목록 조회

**인증**: Bearer JWT
**권한**: ROLE_ADMIN

**Query Parameters**:
| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|------|--------|------|
| staffType | String | 아니오 | 전체 | `DRIVER`, `INTERPRETER` |
| isAvailable | Boolean | 아니오 | - | 배정 가능 여부 |
| language | String | 아니오 | - | 사용 가능 언어 코드 |
| page | int | 아니오 | 0 | 페이지 번호 |
| size | int | 아니오 | 20 | 페이지 크기 |

**Response** (`200 OK`):
```json
{
  "success": true,
  "message": "조회 성공",
  "data": {
    "content": [
      {
        "id": 10,
        "name": "김기사",
        "staffType": "DRIVER",
        "languages": ["ko", "en"],
        "isAvailable": true,
        "currentStatus": "IDLE",
        "activeAssignments": 0,
        "rating": 4.8,
        "totalCompletedJobs": 150
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 30,
    "totalPages": 2
  }
}
```

---

### 5.2 실무자 자동 배정

> 관련 요구사항: ADM-601

#### `POST /api/v1/staff/auto-assign`

**설명**: 여정에 적합한 실무자를 자동 배정 (언어, 가용성, 위치 기반)

**인증**: Bearer JWT
**권한**: ROLE_ADMIN

**Request Body**:
```json
{
  "journeyId": 100,
  "scheduleItemId": 500,
  "requiredStaffType": "DRIVER",
  "requiredLanguages": ["en"],
  "scheduledAt": "2026-03-25T09:00:00+09:00",
  "estimatedDurationMinutes": 120,
  "pickupLocation": {
    "latitude": 37.5665,
    "longitude": 126.9780
  }
}
```

**Response** (`200 OK`):
```json
{
  "success": true,
  "message": "자동 배정 완료",
  "data": {
    "assignmentId": 200,
    "staffId": 10,
    "staffName": "김기사",
    "staffType": "DRIVER",
    "matchScore": 0.95,
    "matchReasons": [
      "언어 일치: en",
      "해당 시간 가용",
      "현재 위치 인접 (3.2km)"
    ]
  }
}
```

**Error Cases**:
| HTTP Status | Error Code | 설명 |
|-------------|-----------|------|
| 404 | STF_001 | 존재하지 않는 여정/일정 |
| 409 | STF_002 | 해당 시간대에 배정 가능한 실무자 없음 |

---

### 5.3 실무자 수동 배정

> 관련 요구사항: ADM-601

#### `POST /api/v1/staff/{staffId}/assign`

**설명**: 특정 실무자를 수동으로 여정에 배정

**인증**: Bearer JWT
**권한**: ROLE_ADMIN

**Path Parameters**:
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| staffId | Long | 예 | 실무자 ID |

**Request Body**:
```json
{
  "journeyId": 100,
  "scheduleItemId": 500,
  "scheduledAt": "2026-03-25T09:00:00+09:00",
  "estimatedDurationMinutes": 120
}
```

**Response** (`201 Created`):
```json
{
  "success": true,
  "message": "배정 완료",
  "data": {
    "assignmentId": 201,
    "staffId": 10,
    "journeyId": 100,
    "scheduleItemId": 500,
    "status": "ASSIGNED"
  }
}
```

**Error Cases**:
| HTTP Status | Error Code | 설명 |
|-------------|-----------|------|
| 404 | STF_010 | 존재하지 않는 실무자 |
| 409 | STF_011 | 해당 시간대 실무자 일정 충돌 |

---

### 5.4 실무자 실시간 위치/상태 조회 (관리자)

> 관련 요구사항: ADM-602

#### `GET /api/v1/staff/realtime`

**설명**: 전체 실무자의 실시간 위치 및 상태 조회

**인증**: Bearer JWT
**권한**: ROLE_ADMIN

**Query Parameters**:
| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|------|--------|------|
| staffType | String | 아니오 | 전체 | `DRIVER`, `INTERPRETER` |
| status | String | 아니오 | 전체 | `IDLE`, `EN_ROUTE`, `ON_DUTY`, `OFFLINE` |

**Response** (`200 OK`):
```json
{
  "success": true,
  "message": "조회 성공",
  "data": [
    {
      "staffId": 10,
      "name": "김기사",
      "staffType": "DRIVER",
      "status": "EN_ROUTE",
      "location": {
        "latitude": 37.5665,
        "longitude": 126.9780,
        "updatedAt": "2026-03-21T14:29:50Z"
      },
      "currentAssignment": {
        "journeyId": 100,
        "patientName": "John Doe",
        "destination": "서울 뷰티 클리닉"
      }
    }
  ]
}
```

---

### 5.5 실무자 위치 업데이트

> 관련 요구사항: ADM-602

#### `PUT /api/v1/staff/me/location`

**설명**: 실무자 본인의 현재 위치를 업데이트

**인증**: Bearer JWT
**권한**: ROLE_STAFF

**Request Body**:
```json
{
  "latitude": 37.5665,
  "longitude": 126.9780
}
```

**Response** (`200 OK`):
```json
{
  "success": true,
  "message": "위치 업데이트 완료",
  "data": {
    "latitude": 37.5665,
    "longitude": 126.9780,
    "updatedAt": "2026-03-21T14:30:00Z"
  }
}
```

---

### 5.6 배정된 실무자 프로필 조회 (환자)

> 관련 요구사항: PAT-303

#### `GET /api/v1/staff/assigned`

**설명**: 환자에게 배정된 실무자(기사/통역사) 프로필 목록 조회

**인증**: Bearer JWT
**권한**: ROLE_PATIENT

**Query Parameters**:
| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|------|--------|------|
| journeyId | Long | 예 | - | 여정 ID |

**Response** (`200 OK`):
```json
{
  "success": true,
  "message": "조회 성공",
  "data": [
    {
      "id": 10,
      "name": "김기사",
      "staffType": "DRIVER",
      "languages": ["ko", "en"],
      "profileImageUrl": "https://cdn.example.com/profiles/10.jpg",
      "phone": "+82-10-1234-5678",
      "vehicleInfo": {
        "type": "SEDAN",
        "plateNumber": "서울 12가 3456"
      },
      "rating": 4.8,
      "assignedSchedules": [
        {
          "scheduleItemId": 500,
          "title": "공항 픽업",
          "scheduledAt": "2026-03-25T09:00:00+09:00"
        }
      ]
    }
  ]
}
```

**Error Cases**:
| HTTP Status | Error Code | 설명 |
|-------------|-----------|------|
| 403 | STF_020 | 해당 여정에 대한 접근 권한 없음 |
| 404 | STF_021 | 존재하지 않는 여정 |

---

## 6. Proposal API

### 6.1 견적서 생성 (관리자)

> 관련 요구사항: ADM-301

#### `POST /api/v1/proposals`

**설명**: 스마트 견적서(Proposal) 생성

**인증**: Bearer JWT
**권한**: ROLE_ADMIN

**Request Body**:
```json
{
  "patientId": 5,
  "title": "서울 성형외과 VIP 패키지",
  "currency": "USD",
  "validUntil": "2026-04-21T23:59:59Z",
  "items": [
    {
      "category": "SURGERY",
      "name": "코 성형 (비절개)",
      "description": "자연스러운 코 라인 교정",
      "unitPrice": 3000.00,
      "quantity": 1
    },
    {
      "category": "CONCIERGE",
      "name": "공항 픽업/샌딩",
      "description": "인천공항 VIP 라운지 포함",
      "unitPrice": 200.00,
      "quantity": 2
    },
    {
      "category": "ACCOMMODATION",
      "name": "리커버리 호텔 (5박)",
      "description": "병원 인근 5성급",
      "unitPrice": 250.00,
      "quantity": 5
    }
  ],
  "discountRate": 5.0,
  "notes": "수술 후 무료 1회 팔로업 포함"
}
```

**Response** (`201 Created`):
```json
{
  "success": true,
  "message": "견적서 생성 완료",
  "data": {
    "id": 50,
    "patientId": 5,
    "title": "서울 성형외과 VIP 패키지",
    "status": "DRAFT",
    "subtotal": 4450.00,
    "discountAmount": 222.50,
    "totalAmount": 4227.50,
    "currency": "USD",
    "validUntil": "2026-04-21T23:59:59Z",
    "createdAt": "2026-03-21T14:30:00Z"
  }
}
```

**Error Cases**:
| HTTP Status | Error Code | 설명 |
|-------------|-----------|------|
| 400 | PRP_001 | 필수 항목 누락 |
| 404 | PRP_002 | 존재하지 않는 환자 |

---

### 6.2 견적서 목록 조회 (관리자)

> 관련 요구사항: ADM-301

#### `GET /api/v1/proposals`

**설명**: 전체 견적서 목록 조회 (관리자)

**인증**: Bearer JWT
**권한**: ROLE_ADMIN

**Query Parameters**:
| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|------|--------|------|
| status | String | 아니오 | 전체 | `DRAFT`, `SENT`, `ACCEPTED`, `REJECTED`, `EXPIRED` |
| patientId | Long | 아니오 | - | 특정 환자 필터 |
| page | int | 아니오 | 0 | 페이지 번호 |
| size | int | 아니오 | 20 | 페이지 크기 |

**Response** (`200 OK`):
```json
{
  "success": true,
  "message": "조회 성공",
  "data": {
    "content": [
      {
        "id": 50,
        "patientId": 5,
        "patientName": "John Doe",
        "title": "서울 성형외과 VIP 패키지",
        "status": "SENT",
        "totalAmount": 4227.50,
        "currency": "USD",
        "validUntil": "2026-04-21T23:59:59Z",
        "createdAt": "2026-03-21T14:30:00Z"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 10,
    "totalPages": 1
  }
}
```

---

### 6.3 견적서 상세 조회

> 관련 요구사항: ADM-301, PAT-402

#### `GET /api/v1/proposals/{proposalId}`

**설명**: 견적서 상세 내용 조회

**인증**: Bearer JWT
**권한**: ROLE_ADMIN, ROLE_PATIENT (본인 것만)

**Path Parameters**:
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| proposalId | Long | 예 | 견적서 ID |

**Response** (`200 OK`):
```json
{
  "success": true,
  "message": "조회 성공",
  "data": {
    "id": 50,
    "patientId": 5,
    "title": "서울 성형외과 VIP 패키지",
    "status": "SENT",
    "currency": "USD",
    "items": [
      {
        "id": 1,
        "category": "SURGERY",
        "name": "코 성형 (비절개)",
        "description": "자연스러운 코 라인 교정",
        "unitPrice": 3000.00,
        "quantity": 1,
        "amount": 3000.00
      },
      {
        "id": 2,
        "category": "CONCIERGE",
        "name": "공항 픽업/샌딩",
        "description": "인천공항 VIP 라운지 포함",
        "unitPrice": 200.00,
        "quantity": 2,
        "amount": 400.00
      },
      {
        "id": 3,
        "category": "ACCOMMODATION",
        "name": "리커버리 호텔 (5박)",
        "description": "병원 인근 5성급",
        "unitPrice": 250.00,
        "quantity": 5,
        "amount": 1250.00
      }
    ],
    "subtotal": 4450.00,
    "discountRate": 5.0,
    "discountAmount": 222.50,
    "totalAmount": 4227.50,
    "validUntil": "2026-04-21T23:59:59Z",
    "notes": "수술 후 무료 1회 팔로업 포함",
    "createdAt": "2026-03-21T14:30:00Z",
    "sentAt": "2026-03-21T15:00:00Z",
    "respondedAt": null
  }
}
```

**Error Cases**:
| HTTP Status | Error Code | 설명 |
|-------------|-----------|------|
| 403 | PRP_010 | 타인의 견적서 접근 시도 |
| 404 | PRP_011 | 존재하지 않는 견적서 |

---

### 6.4 견적서 발송

> 관련 요구사항: ADM-301

#### `POST /api/v1/proposals/{proposalId}/send`

**설명**: 작성된 견적서를 환자에게 발송 (상태: DRAFT -> SENT)

**인증**: Bearer JWT
**권한**: ROLE_ADMIN

**Path Parameters**:
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| proposalId | Long | 예 | 견적서 ID |

**Response** (`200 OK`):
```json
{
  "success": true,
  "message": "견적서 발송 완료",
  "data": {
    "id": 50,
    "status": "SENT",
    "sentAt": "2026-03-21T15:00:00Z"
  }
}
```

**Error Cases**:
| HTTP Status | Error Code | 설명 |
|-------------|-----------|------|
| 400 | PRP_020 | DRAFT 상태가 아닌 견적서는 발송 불가 |
| 404 | PRP_021 | 존재하지 않는 견적서 |

---

### 6.5 견적 요청 (환자)

> 관련 요구사항: PAT-401

#### `POST /api/v1/proposals/request`

**설명**: 환자가 수술/컨시어지 견적을 요청

**인증**: Bearer JWT
**권한**: ROLE_PATIENT

**Request Body**:
```json
{
  "desiredProcedures": ["코 성형", "피부 레이저"],
  "preferredHospitalIds": [1, 2],
  "travelDates": {
    "arrivalDate": "2026-04-15",
    "departureDate": "2026-04-25"
  },
  "accommodationPreference": "5_STAR_HOTEL",
  "conciergeServices": ["AIRPORT_PICKUP", "INTERPRETER", "CITY_TOUR"],
  "budget": {
    "min": 3000,
    "max": 8000,
    "currency": "USD"
  },
  "additionalRequests": "할랄 식단 필요"
}
```

**Response** (`201 Created`):
```json
{
  "success": true,
  "message": "견적 요청 완료",
  "data": {
    "requestId": 30,
    "status": "PENDING",
    "createdAt": "2026-03-21T14:30:00Z"
  }
}
```

**Error Cases**:
| HTTP Status | Error Code | 설명 |
|-------------|-----------|------|
| 400 | PRP_030 | 필수 항목 누락 (여행 날짜 등) |

---

### 6.6 내 견적서 목록 조회 (환자)

> 관련 요구사항: PAT-402

#### `GET /api/v1/proposals/me`

**설명**: 환자 본인에게 발송된 견적서 목록 조회 (비교 용도)

**인증**: Bearer JWT
**권한**: ROLE_PATIENT

**Query Parameters**:
| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|------|--------|------|
| status | String | 아니오 | 전체 | `SENT`, `ACCEPTED`, `REJECTED` |
| page | int | 아니오 | 0 | 페이지 번호 |
| size | int | 아니오 | 20 | 페이지 크기 |

**Response** (`200 OK`):
```json
{
  "success": true,
  "message": "조회 성공",
  "data": {
    "content": [
      {
        "id": 50,
        "title": "서울 성형외과 VIP 패키지",
        "status": "SENT",
        "totalAmount": 4227.50,
        "currency": "USD",
        "validUntil": "2026-04-21T23:59:59Z",
        "itemSummary": {
          "surgeryCount": 1,
          "conciergeCount": 1,
          "accommodationNights": 5
        },
        "createdAt": "2026-03-21T14:30:00Z"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 3,
    "totalPages": 1
  }
}
```

---

### 6.7 견적서 수락/거절 (환자)

> 관련 요구사항: PAT-403

#### `POST /api/v1/proposals/{proposalId}/respond`

**설명**: 환자가 견적서를 수락 또는 거절

**인증**: Bearer JWT
**권한**: ROLE_PATIENT

**Path Parameters**:
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| proposalId | Long | 예 | 견적서 ID |

**Request Body**:
```json
{
  "action": "ACCEPT",
  "message": "이 패키지로 진행하겠습니다"
}
```

> `action`: `ACCEPT` 또는 `REJECT`

**Response** (`200 OK`):
```json
{
  "success": true,
  "message": "견적서 수락 완료",
  "data": {
    "id": 50,
    "status": "ACCEPTED",
    "respondedAt": "2026-03-21T16:00:00Z"
  }
}
```

**Error Cases**:
| HTTP Status | Error Code | 설명 |
|-------------|-----------|------|
| 400 | PRP_040 | 이미 응답한 견적서 |
| 400 | PRP_041 | 유효기간 만료된 견적서 |
| 403 | PRP_042 | 본인 견적서가 아님 |

---

### 6.8 견적서 수정 (관리자)

> 관련 요구사항: ADM-301

#### `PUT /api/v1/proposals/{proposalId}`

**설명**: DRAFT 상태의 견적서 수정

**인증**: Bearer JWT
**권한**: ROLE_ADMIN

**Path Parameters**:
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| proposalId | Long | 예 | 견적서 ID |

**Request Body**: (6.1과 동일한 포맷, `patientId` 제외)

**Response** (`200 OK`):
```json
{
  "success": true,
  "message": "견적서 수정 완료",
  "data": {
    "id": 50,
    "status": "DRAFT",
    "totalAmount": 4500.00,
    "updatedAt": "2026-03-21T16:00:00Z"
  }
}
```

**Error Cases**:
| HTTP Status | Error Code | 설명 |
|-------------|-----------|------|
| 400 | PRP_050 | DRAFT 상태가 아닌 견적서는 수정 불가 |
| 404 | PRP_051 | 존재하지 않는 견적서 |

---

## 7. Journey API

### 7.1 여정 템플릿 목록 조회

> 관련 요구사항: ADM-401

#### `GET /api/v1/journeys/templates`

**설명**: 여정 템플릿 목록 조회 (CMS)

**인증**: Bearer JWT
**권한**: ROLE_ADMIN

**Query Parameters**:
| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|------|--------|------|
| keyword | String | 아니오 | - | 템플릿 이름 검색 |
| category | String | 아니오 | 전체 | `SURGERY`, `TOUR`, `RECOVERY`, `MIXED` |
| page | int | 아니오 | 0 | 페이지 번호 |
| size | int | 아니오 | 20 | 페이지 크기 |

**Response** (`200 OK`):
```json
{
  "success": true,
  "message": "조회 성공",
  "data": {
    "content": [
      {
        "id": 1,
        "name": "서울 성형 VIP 7일 패키지",
        "category": "MIXED",
        "durationDays": 7,
        "itemCount": 15,
        "usageCount": 35,
        "createdAt": "2026-01-15T09:00:00Z"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 10,
    "totalPages": 1
  }
}
```

---

### 7.2 여정 템플릿 생성

> 관련 요구사항: ADM-401

#### `POST /api/v1/journeys/templates`

**설명**: 새 여정 템플릿 생성 (CMS 빌더)

**인증**: Bearer JWT
**권한**: ROLE_ADMIN

**Request Body**:
```json
{
  "name": "서울 성형 VIP 7일 패키지",
  "category": "MIXED",
  "durationDays": 7,
  "items": [
    {
      "dayOffset": 0,
      "timeOffset": "09:00",
      "title": "인천공항 도착 & 픽업",
      "type": "TRANSPORT",
      "description": "VIP 라운지 이용 후 전용차량 픽업",
      "durationMinutes": 90,
      "location": {
        "name": "인천국제공항 제2터미널",
        "address": "인천광역시 중구 ...",
        "latitude": 37.4602,
        "longitude": 126.4407
      },
      "requiredStaff": ["DRIVER", "INTERPRETER"]
    },
    {
      "dayOffset": 0,
      "timeOffset": "11:30",
      "title": "호텔 체크인",
      "type": "ACCOMMODATION",
      "description": "리커버리 전용 호텔",
      "durationMinutes": 60,
      "location": {
        "name": "강남 리커버리 호텔",
        "address": "서울시 강남구 ...",
        "latitude": 37.5172,
        "longitude": 127.0473
      },
      "requiredStaff": ["INTERPRETER"]
    }
  ]
}
```

**Response** (`201 Created`):
```json
{
  "success": true,
  "message": "템플릿 생성 완료",
  "data": {
    "id": 2,
    "name": "서울 성형 VIP 7일 패키지",
    "category": "MIXED",
    "durationDays": 7,
    "itemCount": 2,
    "createdAt": "2026-03-21T14:30:00Z"
  }
}
```

**Error Cases**:
| HTTP Status | Error Code | 설명 |
|-------------|-----------|------|
| 400 | JRN_001 | 필수 항목 누락 |
| 409 | JRN_002 | 동일 이름의 템플릿 존재 |

---

### 7.3 여정 템플릿 상세 조회

> 관련 요구사항: ADM-401

#### `GET /api/v1/journeys/templates/{templateId}`

**설명**: 여정 템플릿 상세 조회

**인증**: Bearer JWT
**권한**: ROLE_ADMIN

**Path Parameters**:
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| templateId | Long | 예 | 템플릿 ID |

**Response** (`200 OK`):
```json
{
  "success": true,
  "message": "조회 성공",
  "data": {
    "id": 1,
    "name": "서울 성형 VIP 7일 패키지",
    "category": "MIXED",
    "durationDays": 7,
    "items": [
      {
        "id": 1,
        "dayOffset": 0,
        "timeOffset": "09:00",
        "title": "인천공항 도착 & 픽업",
        "type": "TRANSPORT",
        "description": "VIP 라운지 이용 후 전용차량 픽업",
        "durationMinutes": 90,
        "location": {
          "name": "인천국제공항 제2터미널",
          "address": "인천광역시 중구 ...",
          "latitude": 37.4602,
          "longitude": 126.4407
        },
        "requiredStaff": ["DRIVER", "INTERPRETER"],
        "order": 1
      }
    ],
    "createdAt": "2026-01-15T09:00:00Z",
    "updatedAt": "2026-03-21T14:30:00Z"
  }
}
```

---

### 7.4 여정 템플릿 수정

> 관련 요구사항: ADM-401

#### `PUT /api/v1/journeys/templates/{templateId}`

**설명**: 여정 템플릿 수정

**인증**: Bearer JWT
**권한**: ROLE_ADMIN

**Path Parameters**:
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| templateId | Long | 예 | 템플릿 ID |

**Request Body**: (7.2와 동일한 포맷)

**Response** (`200 OK`):
```json
{
  "success": true,
  "message": "템플릿 수정 완료",
  "data": {
    "id": 1,
    "name": "서울 성형 VIP 7일 패키지",
    "itemCount": 15,
    "updatedAt": "2026-03-21T16:00:00Z"
  }
}
```

---

### 7.5 여정 템플릿 삭제

> 관련 요구사항: ADM-401

#### `DELETE /api/v1/journeys/templates/{templateId}`

**설명**: 여정 템플릿 삭제

**인증**: Bearer JWT
**권한**: ROLE_ADMIN

**Path Parameters**:
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| templateId | Long | 예 | 템플릿 ID |

**Response** (`204 No Content`)

**Error Cases**:
| HTTP Status | Error Code | 설명 |
|-------------|-----------|------|
| 409 | JRN_010 | 활성 여정에서 사용 중인 템플릿은 삭제 불가 |

---

### 7.6 환자 여정 생성 (템플릿 기반)

> 관련 요구사항: ADM-401, ADM-402

#### `POST /api/v1/journeys`

**설명**: 템플릿 기반으로 환자의 실제 여정을 생성

**인증**: Bearer JWT
**권한**: ROLE_ADMIN

**Request Body**:
```json
{
  "patientId": 5,
  "templateId": 1,
  "startDate": "2026-04-15",
  "title": "John Doe - 서울 성형 VIP",
  "notes": "할랄 식단 필요, 휠체어 이동 보조"
}
```

**Response** (`201 Created`):
```json
{
  "success": true,
  "message": "여정 생성 완료",
  "data": {
    "id": 100,
    "patientId": 5,
    "title": "John Doe - 서울 성형 VIP",
    "status": "PLANNED",
    "startDate": "2026-04-15",
    "endDate": "2026-04-21",
    "scheduleItemCount": 15,
    "createdAt": "2026-03-21T14:30:00Z"
  }
}
```

**Error Cases**:
| HTTP Status | Error Code | 설명 |
|-------------|-----------|------|
| 404 | JRN_020 | 존재하지 않는 환자 또는 템플릿 |
| 409 | JRN_021 | 해당 환자에게 이미 진행 중인 여정이 존재 |

---

### 7.7 여정 목록 조회 (관리자)

> 관련 요구사항: ADM-402

#### `GET /api/v1/journeys`

**설명**: 전체 여정 목록 조회

**인증**: Bearer JWT
**권한**: ROLE_ADMIN

**Query Parameters**:
| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|------|--------|------|
| status | String | 아니오 | 전체 | `PLANNED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED` |
| patientId | Long | 아니오 | - | 특정 환자 필터 |
| startDateFrom | String | 아니오 | - | 시작일 범위 (from) |
| startDateTo | String | 아니오 | - | 시작일 범위 (to) |
| page | int | 아니오 | 0 | 페이지 번호 |
| size | int | 아니오 | 20 | 페이지 크기 |

**Response** (`200 OK`):
```json
{
  "success": true,
  "message": "조회 성공",
  "data": {
    "content": [
      {
        "id": 100,
        "patientId": 5,
        "patientName": "John Doe",
        "title": "John Doe - 서울 성형 VIP",
        "status": "IN_PROGRESS",
        "startDate": "2026-04-15",
        "endDate": "2026-04-21",
        "currentScheduleItem": {
          "id": 505,
          "title": "병원 진료",
          "status": "IN_PROGRESS"
        },
        "progress": 45,
        "assignedStaffCount": 3
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 25,
    "totalPages": 2
  }
}
```

---

### 7.8 여정 상세 조회

> 관련 요구사항: ADM-402, PAT-601

#### `GET /api/v1/journeys/{journeyId}`

**설명**: 여정 상세 정보 및 전체 일정 항목 조회

**인증**: Bearer JWT
**권한**: ROLE_ADMIN, ROLE_PATIENT (본인 여정만), ROLE_STAFF (배정된 여정만)

**Path Parameters**:
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| journeyId | Long | 예 | 여정 ID |

**Response** (`200 OK`):
```json
{
  "success": true,
  "message": "조회 성공",
  "data": {
    "id": 100,
    "patientId": 5,
    "patientName": "John Doe",
    "title": "John Doe - 서울 성형 VIP",
    "status": "IN_PROGRESS",
    "startDate": "2026-04-15",
    "endDate": "2026-04-21",
    "notes": "할랄 식단 필요",
    "scheduleItems": [
      {
        "id": 500,
        "dayNumber": 1,
        "scheduledAt": "2026-04-15T09:00:00+09:00",
        "title": "인천공항 도착 & 픽업",
        "type": "TRANSPORT",
        "description": "VIP 라운지 이용 후 전용차량 픽업",
        "status": "COMPLETED",
        "durationMinutes": 90,
        "location": {
          "name": "인천국제공항 제2터미널",
          "address": "인천광역시 중구 ...",
          "latitude": 37.4602,
          "longitude": 126.4407,
          "googleMapsUrl": "https://maps.google.com/?q=37.4602,126.4407"
        },
        "assignedStaff": [
          {
            "staffId": 10,
            "name": "김기사",
            "staffType": "DRIVER",
            "status": "COMPLETED"
          }
        ],
        "completedAt": "2026-04-15T10:15:00+09:00"
      },
      {
        "id": 505,
        "dayNumber": 2,
        "scheduledAt": "2026-04-16T10:00:00+09:00",
        "title": "병원 진료",
        "type": "MEDICAL",
        "description": "초진 상담 및 수술 전 검사",
        "status": "IN_PROGRESS",
        "durationMinutes": 180,
        "location": {
          "name": "서울 뷰티 클리닉",
          "address": "서울시 강남구 ...",
          "latitude": 37.5172,
          "longitude": 127.0473,
          "googleMapsUrl": "https://maps.google.com/?q=37.5172,127.0473"
        },
        "assignedStaff": [
          {
            "staffId": 11,
            "name": "이통역",
            "staffType": "INTERPRETER",
            "status": "ON_DUTY"
          }
        ],
        "completedAt": null
      }
    ],
    "progress": 45,
    "createdAt": "2026-03-21T14:30:00Z"
  }
}
```

**Error Cases**:
| HTTP Status | Error Code | 설명 |
|-------------|-----------|------|
| 403 | JRN_030 | 접근 권한 없음 |
| 404 | JRN_031 | 존재하지 않는 여정 |

---

### 7.9 일정 항목 수정 (실시간)

> 관련 요구사항: ADM-402

#### `PUT /api/v1/journeys/{journeyId}/schedule-items/{itemId}`

**설명**: 여정 내 일정 항목 수정 (시간, 장소, 설명 등). 수정 시 관련 실무자/환자에게 실시간 알림 발송.

**인증**: Bearer JWT
**권한**: ROLE_ADMIN

**Path Parameters**:
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| journeyId | Long | 예 | 여정 ID |
| itemId | Long | 예 | 일정 항목 ID |

**Request Body**:
```json
{
  "scheduledAt": "2026-04-16T11:00:00+09:00",
  "title": "병원 진료 (시간 변경)",
  "description": "초진 상담 및 수술 전 검사 - 1시간 지연",
  "durationMinutes": 180,
  "location": {
    "name": "서울 뷰티 클리닉",
    "address": "서울시 강남구 ...",
    "latitude": 37.5172,
    "longitude": 127.0473
  },
  "changeReason": "병원 사정으로 1시간 지연"
}
```

**Response** (`200 OK`):
```json
{
  "success": true,
  "message": "일정 수정 완료 (관련자 알림 발송됨)",
  "data": {
    "id": 505,
    "journeyId": 100,
    "scheduledAt": "2026-04-16T11:00:00+09:00",
    "title": "병원 진료 (시간 변경)",
    "status": "SCHEDULED",
    "notifiedUsers": [
      {"userId": 5, "role": "PATIENT", "method": "PUSH"},
      {"userId": 10, "role": "STAFF", "method": "PUSH"},
      {"userId": 11, "role": "STAFF", "method": "PUSH"}
    ],
    "updatedAt": "2026-03-21T16:00:00Z"
  }
}
```

**Error Cases**:
| HTTP Status | Error Code | 설명 |
|-------------|-----------|------|
| 400 | JRN_040 | 이미 완료된 일정은 수정 불가 |
| 404 | JRN_041 | 존재하지 않는 여정 또는 일정 항목 |

---

### 7.10 일정 항목 추가

> 관련 요구사항: ADM-402

#### `POST /api/v1/journeys/{journeyId}/schedule-items`

**설명**: 여정에 새 일정 항목 추가

**인증**: Bearer JWT
**권한**: ROLE_ADMIN

**Path Parameters**:
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| journeyId | Long | 예 | 여정 ID |

**Request Body**:
```json
{
  "scheduledAt": "2026-04-17T14:00:00+09:00",
  "title": "명동 쇼핑 투어",
  "type": "TOUR",
  "description": "수술 후 회복 중 가벼운 관광",
  "durationMinutes": 180,
  "location": {
    "name": "명동 메인 스트리트",
    "address": "서울시 중구 명동 ...",
    "latitude": 37.5636,
    "longitude": 126.9869
  },
  "requiredStaff": ["DRIVER", "INTERPRETER"]
}
```

**Response** (`201 Created`):
```json
{
  "success": true,
  "message": "일정 추가 완료",
  "data": {
    "id": 510,
    "journeyId": 100,
    "scheduledAt": "2026-04-17T14:00:00+09:00",
    "title": "명동 쇼핑 투어",
    "type": "TOUR",
    "status": "SCHEDULED",
    "createdAt": "2026-03-21T16:00:00Z"
  }
}
```

---

### 7.11 일정 항목 삭제

> 관련 요구사항: ADM-402

#### `DELETE /api/v1/journeys/{journeyId}/schedule-items/{itemId}`

**설명**: 여정 내 일정 항목 삭제

**인증**: Bearer JWT
**권한**: ROLE_ADMIN

**Path Parameters**:
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| journeyId | Long | 예 | 여정 ID |
| itemId | Long | 예 | 일정 항목 ID |

**Response** (`204 No Content`)

**Error Cases**:
| HTTP Status | Error Code | 설명 |
|-------------|-----------|------|
| 400 | JRN_050 | 이미 진행/완료된 일정은 삭제 불가 |

---

### 7.12 환자 라이브 타임라인 조회

> 관련 요구사항: PAT-601, PAT-602, PAT-604

#### `GET /api/v1/journeys/me/timeline`

**설명**: 환자 본인의 현재 여정 라이브 타임라인 조회 (실시간 상태 포함, Google Maps 딥링크 포함)

**인증**: Bearer JWT / Magic Link
**권한**: ROLE_PATIENT

**Query Parameters**:
| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|------|--------|------|
| date | String | 아니오 | 오늘 | 특정 날짜 필터 (YYYY-MM-DD) |

**Response** (`200 OK`):
```json
{
  "success": true,
  "message": "조회 성공",
  "data": {
    "journeyId": 100,
    "title": "John Doe - 서울 성형 VIP",
    "currentDate": "2026-04-16",
    "dayNumber": 2,
    "totalDays": 7,
    "items": [
      {
        "id": 505,
        "scheduledAt": "2026-04-16T10:00:00+09:00",
        "title": "병원 진료",
        "type": "MEDICAL",
        "status": "IN_PROGRESS",
        "description": "초진 상담 및 수술 전 검사",
        "location": {
          "name": "서울 뷰티 클리닉",
          "googleMapsUrl": "https://maps.google.com/?q=37.5172,127.0473"
        },
        "assignedStaff": {
          "interpreter": {
            "name": "이통역",
            "phone": "+82-10-5678-1234"
          }
        }
      },
      {
        "id": 506,
        "scheduledAt": "2026-04-16T14:00:00+09:00",
        "title": "호텔 복귀",
        "type": "TRANSPORT",
        "status": "SCHEDULED",
        "location": {
          "name": "강남 리커버리 호텔",
          "googleMapsUrl": "https://maps.google.com/?q=37.5172,127.0473"
        },
        "assignedStaff": {
          "driver": {
            "name": "김기사",
            "phone": "+82-10-1234-5678"
          }
        }
      }
    ]
  }
}
```

---

### 7.13 실무자 당일 업무 리스트 (To-do)

> 관련 요구사항: STA-201

#### `GET /api/v1/journeys/staff/me/today`

**설명**: 실무자 본인의 당일 배정된 업무 리스트

**인증**: Bearer JWT
**권한**: ROLE_STAFF

**Query Parameters**:
| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|------|--------|------|
| date | String | 아니오 | 오늘 | 특정 날짜 (YYYY-MM-DD) |

**Response** (`200 OK`):
```json
{
  "success": true,
  "message": "조회 성공",
  "data": {
    "date": "2026-04-16",
    "totalTasks": 3,
    "completedTasks": 1,
    "tasks": [
      {
        "assignmentId": 200,
        "scheduleItemId": 505,
        "journeyId": 100,
        "scheduledAt": "2026-04-16T10:00:00+09:00",
        "title": "병원 진료 - 통역",
        "type": "MEDICAL",
        "status": "COMPLETED",
        "patient": {
          "id": 5,
          "name": "John Doe",
          "nationality": "US"
        },
        "location": {
          "name": "서울 뷰티 클리닉",
          "address": "서울시 강남구 ...",
          "googleMapsUrl": "https://maps.google.com/?q=37.5172,127.0473"
        },
        "durationMinutes": 180,
        "completedAt": "2026-04-16T13:00:00+09:00"
      },
      {
        "assignmentId": 201,
        "scheduleItemId": 506,
        "journeyId": 100,
        "scheduledAt": "2026-04-16T14:00:00+09:00",
        "title": "호텔 복귀 이동",
        "type": "TRANSPORT",
        "status": "ASSIGNED",
        "patient": {
          "id": 5,
          "name": "John Doe",
          "nationality": "US"
        },
        "location": {
          "name": "강남 리커버리 호텔",
          "googleMapsUrl": "https://maps.google.com/?q=37.5172,127.0473"
        },
        "durationMinutes": 30,
        "completedAt": null
      }
    ]
  }
}
```

---

### 7.14 환자 특이사항 조회 (실무자)

> 관련 요구사항: STA-301

#### `GET /api/v1/journeys/{journeyId}/patient-notice`

**설명**: 실무자가 배정된 여정의 환자 특이사항(알레르기, 주의사항 등) 조회

**인증**: Bearer JWT
**권한**: ROLE_STAFF (해당 여정에 배정된 실무자만)

**Path Parameters**:
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| journeyId | Long | 예 | 여정 ID |

**Response** (`200 OK`):
```json
{
  "success": true,
  "message": "조회 성공",
  "data": {
    "patientId": 5,
    "patientName": "John Doe",
    "nationality": "US",
    "language": "en",
    "allergies": [
      {
        "type": "DRUG",
        "name": "Penicillin",
        "severity": "SEVERE"
      }
    ],
    "specialNotes": "할랄 식단 필요, 휠체어 이동 보조",
    "emergencyContact": {
      "name": "Jane Doe",
      "relationship": "SPOUSE",
      "phone": "+1-555-0100"
    }
  }
}
```

**Error Cases**:
| HTTP Status | Error Code | 설명 |
|-------------|-----------|------|
| 403 | JRN_060 | 해당 여정에 배정되지 않은 실무자 |

---

### 7.15 원터치 상태 업데이트 (실무자)

> 관련 요구사항: STA-401

#### `PATCH /api/v1/journeys/{journeyId}/schedule-items/{itemId}/status`

**설명**: 실무자가 배정된 일정의 상태를 원터치로 업데이트. 변경 시 관리자 대시보드 + 환자 타임라인에 5초 이내 실시간 반영.

**인증**: Bearer JWT
**권한**: ROLE_STAFF (해당 일정에 배정된 실무자만)

**Path Parameters**:
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| journeyId | Long | 예 | 여정 ID |
| itemId | Long | 예 | 일정 항목 ID |

**Request Body**:
```json
{
  "status": "IN_PROGRESS",
  "note": "환자 픽업 완료, 병원으로 이동 중"
}
```

> `status` 가능값: `EN_ROUTE` (이동중) -> `ARRIVED` (도착) -> `IN_PROGRESS` (진행중) -> `COMPLETED` (완료)

**Response** (`200 OK`):
```json
{
  "success": true,
  "message": "상태 업데이트 완료",
  "data": {
    "scheduleItemId": 505,
    "previousStatus": "ARRIVED",
    "newStatus": "IN_PROGRESS",
    "updatedAt": "2026-04-16T10:15:00+09:00",
    "updatedBy": {
      "staffId": 10,
      "name": "김기사"
    }
  }
}
```

**Error Cases**:
| HTTP Status | Error Code | 설명 |
|-------------|-----------|------|
| 400 | JRN_070 | 유효하지 않은 상태 전이 (예: COMPLETED -> IN_PROGRESS) |
| 403 | JRN_071 | 해당 일정에 배정되지 않은 실무자 |

---

### 7.16 현장 사진 업로드 (실무자)

> 관련 요구사항: STA-402

#### `POST /api/v1/journeys/{journeyId}/schedule-items/{itemId}/photos`

**설명**: 실무자가 현장 사진을 업로드 (증거 사진)

**인증**: Bearer JWT
**권한**: ROLE_STAFF (해당 일정에 배정된 실무자만)

**Path Parameters**:
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| journeyId | Long | 예 | 여정 ID |
| itemId | Long | 예 | 일정 항목 ID |

**Request Body** (`multipart/form-data`):
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| photos | File[] | 예 | 사진 파일 (최대 5장, 각 10MB) |
| caption | String | 아니오 | 사진 설명 |

**Response** (`201 Created`):
```json
{
  "success": true,
  "message": "사진 업로드 완료",
  "data": {
    "photos": [
      {
        "id": 300,
        "url": "https://cdn.example.com/journey/100/505/photo1.jpg",
        "thumbnailUrl": "https://cdn.example.com/journey/100/505/photo1_thumb.jpg",
        "caption": "환자 픽업 완료",
        "uploadedAt": "2026-04-16T10:16:00+09:00"
      }
    ]
  }
}
```

**Error Cases**:
| HTTP Status | Error Code | 설명 |
|-------------|-----------|------|
| 400 | JRN_080 | 파일 크기 초과 또는 지원하지 않는 형식 |
| 403 | JRN_081 | 해당 일정에 배정되지 않은 실무자 |

---

### 7.17 지도 앱 딥링크 조회 (실무자)

> 관련 요구사항: STA-501

#### `GET /api/v1/journeys/{journeyId}/schedule-items/{itemId}/navigation`

**설명**: 일정 항목의 위치로 네비게이션 딥링크 생성

**인증**: Bearer JWT
**권한**: ROLE_STAFF

**Path Parameters**:
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| journeyId | Long | 예 | 여정 ID |
| itemId | Long | 예 | 일정 항목 ID |

**Query Parameters**:
| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|------|--------|------|
| platform | String | 아니오 | google | `google`, `kakao`, `naver`, `apple` |

**Response** (`200 OK`):
```json
{
  "success": true,
  "message": "조회 성공",
  "data": {
    "destination": {
      "name": "서울 뷰티 클리닉",
      "latitude": 37.5172,
      "longitude": 127.0473
    },
    "deepLinks": {
      "google": "https://www.google.com/maps/dir/?api=1&destination=37.5172,127.0473",
      "kakao": "kakaomap://route?ep=37.5172,127.0473",
      "naver": "nmap://route/car?dlat=37.5172&dlng=127.0473&dname=서울뷰티클리닉",
      "apple": "maps://?daddr=37.5172,127.0473"
    }
  }
}
```

---

## 8. Chat API

### 8.1 채팅방 생성

> 관련 요구사항: ADM-501, PAT-501, STA-601

#### `POST /api/v1/chat/rooms`

**설명**: 1:1 채팅방 생성 (에이전시-환자, 에이전시-실무자)

**인증**: Bearer JWT
**권한**: ROLE_ADMIN

**Request Body**:
```json
{
  "type": "PATIENT_AGENCY",
  "participants": [
    {"userId": 5, "role": "PATIENT"},
    {"userId": 1, "role": "ADMIN"}
  ],
  "journeyId": 100,
  "language": "en"
}
```

> `type`: `PATIENT_AGENCY`, `STAFF_AGENCY`, `PATIENT_STAFF`

**Response** (`201 Created`):
```json
{
  "success": true,
  "message": "채팅방 생성 완료",
  "data": {
    "roomId": "room-100-pat5",
    "type": "PATIENT_AGENCY",
    "participants": [
      {"userId": 5, "name": "John Doe", "role": "PATIENT"},
      {"userId": 1, "name": "관리자", "role": "ADMIN"}
    ],
    "createdAt": "2026-03-21T14:30:00Z"
  }
}
```

**Error Cases**:
| HTTP Status | Error Code | 설명 |
|-------------|-----------|------|
| 409 | CHT_001 | 동일 참여자 간 이미 채팅방이 존재 |

---

### 8.2 채팅방 목록 조회

> 관련 요구사항: ADM-501, PAT-501, STA-601

#### `GET /api/v1/chat/rooms`

**설명**: 내가 참여 중인 채팅방 목록 조회

**인증**: Bearer JWT
**권한**: ROLE_ADMIN, ROLE_PATIENT, ROLE_STAFF

**Query Parameters**:
| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|------|--------|------|
| type | String | 아니오 | 전체 | `PATIENT_AGENCY`, `STAFF_AGENCY`, `PATIENT_STAFF` |
| page | int | 아니오 | 0 | 페이지 번호 |
| size | int | 아니오 | 20 | 페이지 크기 |

**Response** (`200 OK`):
```json
{
  "success": true,
  "message": "조회 성공",
  "data": {
    "content": [
      {
        "roomId": "room-100-pat5",
        "type": "PATIENT_AGENCY",
        "otherParticipant": {
          "userId": 5,
          "name": "John Doe",
          "profileImageUrl": "https://cdn.example.com/profiles/5.jpg"
        },
        "lastMessage": {
          "content": "내일 일정 확인 부탁드립니다",
          "sentAt": "2026-03-21T14:25:00Z",
          "isTranslated": true
        },
        "unreadCount": 3,
        "updatedAt": "2026-03-21T14:25:00Z"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 5,
    "totalPages": 1
  }
}
```

---

### 8.3 채팅 메시지 목록 조회

> 관련 요구사항: ADM-501, PAT-501, STA-601

#### `GET /api/v1/chat/rooms/{roomId}/messages`

**설명**: 특정 채팅방의 메시지 이력 조회 (커서 기반 페이지네이션)

**인증**: Bearer JWT
**권한**: ROLE_ADMIN, ROLE_PATIENT, ROLE_STAFF (해당 채팅방 참여자만)

**Path Parameters**:
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| roomId | String | 예 | 채팅방 ID |

**Query Parameters**:
| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|------|--------|------|
| cursor | String | 아니오 | - | 마지막 메시지 ID (이전 메시지 조회) |
| size | int | 아니오 | 50 | 조회 개수 |

**Response** (`200 OK`):
```json
{
  "success": true,
  "message": "조회 성공",
  "data": {
    "messages": [
      {
        "id": "msg-001",
        "roomId": "room-100-pat5",
        "senderId": 5,
        "senderName": "John Doe",
        "senderRole": "PATIENT",
        "type": "TEXT",
        "content": "What time is my appointment tomorrow?",
        "translatedContent": {
          "ko": "내일 진료 시간이 언제인가요?"
        },
        "sentAt": "2026-03-21T14:20:00Z",
        "readBy": [1]
      },
      {
        "id": "msg-002",
        "roomId": "room-100-pat5",
        "senderId": 1,
        "senderName": "관리자",
        "senderRole": "ADMIN",
        "type": "TEXT",
        "content": "내일 오전 10시에 서울 뷰티 클리닉 진료 예정입니다.",
        "translatedContent": {
          "en": "Your appointment is at 10 AM tomorrow at Seoul Beauty Clinic."
        },
        "sentAt": "2026-03-21T14:22:00Z",
        "readBy": [1, 5]
      }
    ],
    "nextCursor": "msg-000",
    "hasMore": true
  }
}
```

---

### 8.4 채팅 메시지 전송 (REST)

> 관련 요구사항: PAT-501, ADM-502, STA-601

#### `POST /api/v1/chat/rooms/{roomId}/messages`

**설명**: 채팅 메시지 전송 (텍스트). 자동 번역 지원.

**인증**: Bearer JWT
**권한**: ROLE_ADMIN, ROLE_PATIENT, ROLE_STAFF (해당 채팅방 참여자만)

**Path Parameters**:
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| roomId | String | 예 | 채팅방 ID |

**Request Body**:
```json
{
  "type": "TEXT",
  "content": "What time is my appointment?",
  "language": "en"
}
```

**Response** (`201 Created`):
```json
{
  "success": true,
  "message": "전송 완료",
  "data": {
    "id": "msg-003",
    "roomId": "room-100-pat5",
    "type": "TEXT",
    "content": "What time is my appointment?",
    "translatedContent": {
      "ko": "진료 시간이 언제인가요?"
    },
    "sentAt": "2026-03-21T14:30:00Z"
  }
}
```

---

### 8.5 채팅 파일/이미지 전송

> 관련 요구사항: PAT-502

#### `POST /api/v1/chat/rooms/{roomId}/messages/file`

**설명**: 의료 사진 및 문서 보안 전송

**인증**: Bearer JWT
**권한**: ROLE_ADMIN, ROLE_PATIENT, ROLE_STAFF (해당 채팅방 참여자만)

**Path Parameters**:
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| roomId | String | 예 | 채팅방 ID |

**Request Body** (`multipart/form-data`):
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| file | File | 예 | 파일 (최대 20MB, jpg/png/pdf) |
| caption | String | 아니오 | 파일 설명 |
| isSecure | Boolean | 아니오 | 보안 전송 여부 (의료 사진 등) |

**Response** (`201 Created`):
```json
{
  "success": true,
  "message": "파일 전송 완료",
  "data": {
    "id": "msg-004",
    "roomId": "room-100-pat5",
    "type": "FILE",
    "file": {
      "id": 400,
      "fileName": "xray_front.jpg",
      "fileSize": 2048000,
      "mimeType": "image/jpeg",
      "url": "https://cdn.example.com/secure/chat/room-100-pat5/xray_front.jpg",
      "isSecure": true,
      "expiresAt": "2026-04-21T14:30:00Z"
    },
    "caption": "수술 전 X-ray 사진",
    "sentAt": "2026-03-21T14:35:00Z"
  }
}
```

**Error Cases**:
| HTTP Status | Error Code | 설명 |
|-------------|-----------|------|
| 400 | CHT_010 | 파일 크기 초과 또는 지원하지 않는 형식 |
| 403 | CHT_011 | 해당 채팅방 참여자가 아님 |

---

### 8.6 메시지 읽음 처리

> 관련 요구사항: PAT-501, ADM-501

#### `POST /api/v1/chat/rooms/{roomId}/read`

**설명**: 채팅방의 메시지를 읽음 처리

**인증**: Bearer JWT
**권한**: ROLE_ADMIN, ROLE_PATIENT, ROLE_STAFF

**Path Parameters**:
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| roomId | String | 예 | 채팅방 ID |

**Request Body**:
```json
{
  "lastReadMessageId": "msg-003"
}
```

**Response** (`200 OK`):
```json
{
  "success": true,
  "message": "읽음 처리 완료",
  "data": {
    "roomId": "room-100-pat5",
    "unreadCount": 0
  }
}
```

---

### 8.7 관리자 멀티챗 관제 조회

> 관련 요구사항: ADM-501

#### `GET /api/v1/chat/admin/monitor`

**설명**: 관리자가 전체 채팅방의 현황을 한눈에 모니터링

**인증**: Bearer JWT
**권한**: ROLE_ADMIN

**Query Parameters**:
| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|------|--------|------|
| hasUnread | Boolean | 아니오 | - | 미읽은 메시지가 있는 방만 |
| type | String | 아니오 | 전체 | 채팅방 타입 필터 |
| page | int | 아니오 | 0 | 페이지 번호 |
| size | int | 아니오 | 20 | 페이지 크기 |

**Response** (`200 OK`):
```json
{
  "success": true,
  "message": "조회 성공",
  "data": {
    "totalRooms": 25,
    "activeRooms": 15,
    "totalUnreadMessages": 42,
    "content": [
      {
        "roomId": "room-100-pat5",
        "type": "PATIENT_AGENCY",
        "patient": {
          "id": 5,
          "name": "John Doe",
          "nationality": "US"
        },
        "journeyId": 100,
        "journeyTitle": "John Doe - 서울 성형 VIP",
        "lastMessage": {
          "content": "What time is my appointment?",
          "translatedContent": "진료 시간이 언제인가요?",
          "sentAt": "2026-03-21T14:30:00Z"
        },
        "unreadCount": 3,
        "isUrgent": false
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 25,
    "totalPages": 2
  }
}
```

---

### 8.8 긴급 호출 (SOS)

> 관련 요구사항: STA-602

#### `POST /api/v1/chat/sos`

**설명**: 실무자가 긴급 상황 시 SOS 호출 (관리자에게 즉시 알림)

**인증**: Bearer JWT
**권한**: ROLE_STAFF

**Request Body**:
```json
{
  "journeyId": 100,
  "scheduleItemId": 505,
  "urgencyLevel": "CRITICAL",
  "message": "환자 상태 급변, 응급실 이송 필요",
  "location": {
    "latitude": 37.5665,
    "longitude": 126.9780
  }
}
```

> `urgencyLevel`: `WARNING`, `HIGH`, `CRITICAL`

**Response** (`201 Created`):
```json
{
  "success": true,
  "message": "SOS 호출 완료 - 관리자에게 즉시 알림됨",
  "data": {
    "sosId": 10,
    "status": "ACTIVE",
    "createdAt": "2026-04-16T14:30:00+09:00",
    "notifiedAdmins": 3
  }
}
```

---

## 9. Notification API

### 9.1 알림 목록 조회

> 관련 요구사항: ADM-102, PAT-503, PAT-603, STA-202

#### `GET /api/v1/notifications`

**설명**: 현재 사용자의 알림 목록 조회

**인증**: Bearer JWT
**권한**: ROLE_ADMIN, ROLE_PATIENT, ROLE_STAFF

**Query Parameters**:
| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|------|--------|------|
| isRead | Boolean | 아니오 | - | 읽음 여부 필터 |
| type | String | 아니오 | 전체 | `SCHEDULE_CHANGE`, `ASSIGNMENT`, `SOS`, `SYSTEM`, `CHAT`, `PROPOSAL` |
| page | int | 아니오 | 0 | 페이지 번호 |
| size | int | 아니오 | 20 | 페이지 크기 |

**Response** (`200 OK`):
```json
{
  "success": true,
  "message": "조회 성공",
  "data": {
    "content": [
      {
        "id": 1000,
        "type": "SCHEDULE_CHANGE",
        "title": "일정 변경 알림",
        "body": "4월 16일 병원 진료 시간이 10:00 -> 11:00로 변경되었습니다.",
        "data": {
          "journeyId": 100,
          "scheduleItemId": 505
        },
        "isRead": false,
        "createdAt": "2026-03-21T16:00:00Z"
      },
      {
        "id": 1001,
        "type": "ASSIGNMENT",
        "title": "새 배정 알림",
        "body": "4월 16일 14:00 호텔 복귀 이동이 배정되었습니다.",
        "data": {
          "assignmentId": 201,
          "journeyId": 100
        },
        "isRead": true,
        "createdAt": "2026-03-21T15:00:00Z"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 15,
    "totalPages": 1
  }
}
```

---

### 9.2 알림 읽음 처리

> 관련 요구사항: ADM-102, PAT-503, STA-202

#### `PATCH /api/v1/notifications/{notificationId}/read`

**설명**: 특정 알림을 읽음 처리

**인증**: Bearer JWT
**권한**: ROLE_ADMIN, ROLE_PATIENT, ROLE_STAFF

**Path Parameters**:
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| notificationId | Long | 예 | 알림 ID |

**Response** (`200 OK`):
```json
{
  "success": true,
  "message": "읽음 처리 완료",
  "data": {
    "id": 1000,
    "isRead": true,
    "readAt": "2026-03-21T16:10:00Z"
  }
}
```

---

### 9.3 알림 전체 읽음 처리

> 관련 요구사항: ADM-102, PAT-503, STA-202

#### `PATCH /api/v1/notifications/read-all`

**설명**: 모든 미읽은 알림을 일괄 읽음 처리

**인증**: Bearer JWT
**권한**: ROLE_ADMIN, ROLE_PATIENT, ROLE_STAFF

**Response** (`200 OK`):
```json
{
  "success": true,
  "message": "전체 읽음 처리 완료",
  "data": {
    "readCount": 5
  }
}
```

---

### 9.4 긴급 알림 조회 (관리자)

> 관련 요구사항: ADM-102

#### `GET /api/v1/notifications/alerts`

**설명**: 관리자용 긴급 상황 알림 센터 (SOS, 일정 지연, 시스템 경고)

**인증**: Bearer JWT
**권한**: ROLE_ADMIN

**Query Parameters**:
| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|------|--------|------|
| status | String | 아니오 | 전체 | `ACTIVE`, `ACKNOWLEDGED`, `RESOLVED` |
| urgencyLevel | String | 아니오 | 전체 | `WARNING`, `HIGH`, `CRITICAL` |
| page | int | 아니오 | 0 | 페이지 번호 |
| size | int | 아니오 | 20 | 페이지 크기 |

**Response** (`200 OK`):
```json
{
  "success": true,
  "message": "조회 성공",
  "data": {
    "content": [
      {
        "id": 500,
        "type": "SOS",
        "urgencyLevel": "CRITICAL",
        "status": "ACTIVE",
        "title": "긴급 SOS - 김기사",
        "body": "환자 상태 급변, 응급실 이송 필요",
        "source": {
          "staffId": 10,
          "staffName": "김기사",
          "journeyId": 100,
          "patientName": "John Doe"
        },
        "location": {
          "latitude": 37.5665,
          "longitude": 126.9780
        },
        "createdAt": "2026-04-16T14:30:00+09:00"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 3,
    "totalPages": 1
  }
}
```

---

### 9.5 긴급 알림 상태 변경 (관리자)

> 관련 요구사항: ADM-102

#### `PATCH /api/v1/notifications/alerts/{alertId}/status`

**설명**: 긴급 알림 상태를 변경 (확인/해결)

**인증**: Bearer JWT
**권한**: ROLE_ADMIN

**Path Parameters**:
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| alertId | Long | 예 | 알림 ID |

**Request Body**:
```json
{
  "status": "ACKNOWLEDGED",
  "note": "응급실 이송 지시 완료"
}
```

**Response** (`200 OK`):
```json
{
  "success": true,
  "message": "상태 변경 완료",
  "data": {
    "id": 500,
    "status": "ACKNOWLEDGED",
    "acknowledgedBy": 1,
    "acknowledgedAt": "2026-04-16T14:35:00+09:00"
  }
}
```

---

### 9.6 푸시 알림 디바이스 토큰 등록

> 관련 요구사항: PAT-603, STA-202

#### `POST /api/v1/notifications/devices`

**설명**: FCM/Web Push 디바이스 토큰 등록

**인증**: Bearer JWT
**권한**: ROLE_ADMIN, ROLE_PATIENT, ROLE_STAFF

**Request Body**:
```json
{
  "token": "fcm-token-string...",
  "platform": "WEB",
  "deviceName": "Chrome on MacOS"
}
```

> `platform`: `WEB`, `ANDROID`, `IOS`

**Response** (`201 Created`):
```json
{
  "success": true,
  "message": "디바이스 등록 완료",
  "data": {
    "id": 50,
    "platform": "WEB",
    "registeredAt": "2026-03-21T14:30:00Z"
  }
}
```

---

## 10. Aftercare API

### 10.1 사후 관리 가이드 생성 (관리자)

> 관련 요구사항: ADM-701

#### `POST /api/v1/aftercare/guides`

**설명**: 수술 후 사후 관리 가이드 생성 및 환자에게 배포

**인증**: Bearer JWT
**권한**: ROLE_ADMIN

**Request Body**:
```json
{
  "patientId": 5,
  "journeyId": 100,
  "title": "코 성형 수술 후 관리 가이드",
  "procedureType": "RHINOPLASTY",
  "language": "en",
  "sections": [
    {
      "title": "Day 1-3: Immediate Post-Op Care",
      "content": "Keep your head elevated at all times...",
      "order": 1
    },
    {
      "title": "Day 4-7: Recovery Phase",
      "content": "You may gently clean around the nose...",
      "order": 2
    },
    {
      "title": "Medications",
      "content": "Take prescribed antibiotics as directed...",
      "order": 3
    }
  ],
  "dosSchedule": [
    {
      "medicationName": "Amoxicillin 500mg",
      "frequency": "3 times daily",
      "duration": "7 days",
      "startDate": "2026-04-17"
    }
  ],
  "followUpDates": [
    {
      "date": "2026-04-22",
      "description": "실밥 제거"
    },
    {
      "date": "2026-05-15",
      "description": "경과 확인 (원격)"
    }
  ]
}
```

**Response** (`201 Created`):
```json
{
  "success": true,
  "message": "가이드 생성 및 배포 완료",
  "data": {
    "id": 20,
    "patientId": 5,
    "title": "코 성형 수술 후 관리 가이드",
    "status": "PUBLISHED",
    "publishedAt": "2026-03-21T14:30:00Z"
  }
}
```

**Error Cases**:
| HTTP Status | Error Code | 설명 |
|-------------|-----------|------|
| 400 | AFT_001 | 필수 항목 누락 |
| 404 | AFT_002 | 존재하지 않는 환자 또는 여정 |

---

### 10.2 사후 관리 가이드 조회 (환자)

> 관련 요구사항: PAT-701

#### `GET /api/v1/aftercare/guides/me`

**설명**: 환자 본인의 사후 관리 가이드 목록 조회

**인증**: Bearer JWT / Magic Link
**권한**: ROLE_PATIENT

**Response** (`200 OK`):
```json
{
  "success": true,
  "message": "조회 성공",
  "data": [
    {
      "id": 20,
      "title": "코 성형 수술 후 관리 가이드",
      "procedureType": "RHINOPLASTY",
      "sections": [
        {
          "title": "Day 1-3: Immediate Post-Op Care",
          "content": "Keep your head elevated at all times...",
          "order": 1
        }
      ],
      "dosSchedule": [
        {
          "medicationName": "Amoxicillin 500mg",
          "frequency": "3 times daily",
          "duration": "7 days",
          "startDate": "2026-04-17"
        }
      ],
      "followUpDates": [
        {
          "date": "2026-04-22",
          "description": "실밥 제거"
        }
      ],
      "publishedAt": "2026-03-21T14:30:00Z"
    }
  ]
}
```

---

### 10.3 사후 관리 가이드 상세 조회

> 관련 요구사항: PAT-701, ADM-701

#### `GET /api/v1/aftercare/guides/{guideId}`

**설명**: 사후 관리 가이드 상세 조회

**인증**: Bearer JWT
**권한**: ROLE_ADMIN, ROLE_PATIENT (본인 것만)

**Path Parameters**:
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| guideId | Long | 예 | 가이드 ID |

**Response** (`200 OK`): (10.2의 단일 객체와 동일)

---

### 10.4 인보이스 생성 (관리자)

> 관련 요구사항: ADM-702

#### `POST /api/v1/aftercare/invoices`

**설명**: 정산 인보이스 발행

**인증**: Bearer JWT
**권한**: ROLE_ADMIN

**Request Body**:
```json
{
  "patientId": 5,
  "journeyId": 100,
  "proposalId": 50,
  "invoiceNumber": "INV-2026-0001",
  "currency": "USD",
  "items": [
    {
      "description": "코 성형 (비절개)",
      "amount": 3000.00
    },
    {
      "description": "공항 픽업/샌딩 x 2",
      "amount": 400.00
    },
    {
      "description": "리커버리 호텔 5박",
      "amount": 1250.00
    },
    {
      "description": "통역 서비스",
      "amount": 500.00
    }
  ],
  "discountAmount": 257.50,
  "taxRate": 10.0,
  "notes": "수술 후 1회 무료 팔로업 포함",
  "dueDate": "2026-05-15"
}
```

**Response** (`201 Created`):
```json
{
  "success": true,
  "message": "인보이스 발행 완료",
  "data": {
    "id": 60,
    "invoiceNumber": "INV-2026-0001",
    "subtotal": 5150.00,
    "discountAmount": 257.50,
    "taxAmount": 489.25,
    "totalAmount": 5381.75,
    "currency": "USD",
    "status": "ISSUED",
    "dueDate": "2026-05-15",
    "issuedAt": "2026-03-21T14:30:00Z"
  }
}
```

**Error Cases**:
| HTTP Status | Error Code | 설명 |
|-------------|-----------|------|
| 409 | AFT_010 | 중복 인보이스 번호 |
| 404 | AFT_011 | 존재하지 않는 환자/여정/견적서 |

---

### 10.5 인보이스 목록 조회 (관리자)

> 관련 요구사항: ADM-702

#### `GET /api/v1/aftercare/invoices`

**설명**: 전체 인보이스 목록 조회

**인증**: Bearer JWT
**권한**: ROLE_ADMIN

**Query Parameters**:
| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|------|--------|------|
| status | String | 아니오 | 전체 | `ISSUED`, `PAID`, `OVERDUE`, `CANCELLED` |
| patientId | Long | 아니오 | - | 특정 환자 필터 |
| page | int | 아니오 | 0 | 페이지 번호 |
| size | int | 아니오 | 20 | 페이지 크기 |

**Response** (`200 OK`):
```json
{
  "success": true,
  "message": "조회 성공",
  "data": {
    "content": [
      {
        "id": 60,
        "invoiceNumber": "INV-2026-0001",
        "patientId": 5,
        "patientName": "John Doe",
        "totalAmount": 5381.75,
        "currency": "USD",
        "status": "ISSUED",
        "dueDate": "2026-05-15",
        "issuedAt": "2026-03-21T14:30:00Z"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 10,
    "totalPages": 1
  }
}
```

---

### 10.6 내 영수증/인보이스 조회 (환자)

> 관련 요구사항: PAT-702

#### `GET /api/v1/aftercare/invoices/me`

**설명**: 환자 본인의 영수증 및 인보이스 내역 조회

**인증**: Bearer JWT
**권한**: ROLE_PATIENT

**Response** (`200 OK`):
```json
{
  "success": true,
  "message": "조회 성공",
  "data": [
    {
      "id": 60,
      "invoiceNumber": "INV-2026-0001",
      "totalAmount": 5381.75,
      "currency": "USD",
      "status": "ISSUED",
      "items": [
        {
          "description": "코 성형 (비절개)",
          "amount": 3000.00
        },
        {
          "description": "공항 픽업/샌딩 x 2",
          "amount": 400.00
        }
      ],
      "dueDate": "2026-05-15",
      "issuedAt": "2026-03-21T14:30:00Z",
      "downloadUrl": "https://cdn.example.com/invoices/INV-2026-0001.pdf"
    }
  ]
}
```

---

### 10.7 업무 종료 리포트 제출 (실무자)

> 관련 요구사항: STA-701

#### `POST /api/v1/aftercare/staff-reports`

**설명**: 실무자가 배정 업무 종료 후 리포트를 제출

**인증**: Bearer JWT
**권한**: ROLE_STAFF

**Request Body**:
```json
{
  "assignmentId": 200,
  "journeyId": 100,
  "summary": "인천공항 픽업 후 호텔까지 안전하게 이동 완료",
  "startedAt": "2026-04-15T09:00:00+09:00",
  "completedAt": "2026-04-15T10:15:00+09:00",
  "actualDurationMinutes": 75,
  "incidents": [],
  "patientCondition": "GOOD",
  "photoIds": [300, 301]
}
```

**Response** (`201 Created`):
```json
{
  "success": true,
  "message": "리포트 제출 완료",
  "data": {
    "id": 80,
    "assignmentId": 200,
    "status": "SUBMITTED",
    "submittedAt": "2026-04-15T10:20:00+09:00"
  }
}
```

**Error Cases**:
| HTTP Status | Error Code | 설명 |
|-------------|-----------|------|
| 400 | AFT_020 | 아직 완료되지 않은 배정에 대한 리포트 |
| 409 | AFT_021 | 이미 리포트가 제출됨 |

---

## 11. Dashboard API

### 11.1 운영 현황 요약 (Overview)

> 관련 요구사항: ADM-101

#### `GET /api/v1/dashboard/overview`

**설명**: 관리자 대시보드 운영 현황 요약 정보

**인증**: Bearer JWT
**권한**: ROLE_ADMIN

**Response** (`200 OK`):
```json
{
  "success": true,
  "message": "조회 성공",
  "data": {
    "todaySummary": {
      "activeJourneys": 12,
      "todayScheduleItems": 35,
      "completedItems": 20,
      "inProgressItems": 8,
      "upcomingItems": 7
    },
    "staffSummary": {
      "totalStaff": 30,
      "onDuty": 15,
      "available": 10,
      "offline": 5
    },
    "patientSummary": {
      "totalPatients": 50,
      "activePatients": 12,
      "pendingVerification": 3
    },
    "alertSummary": {
      "activeAlerts": 2,
      "criticalAlerts": 1,
      "unresolvedSOS": 0
    },
    "proposalSummary": {
      "pendingProposals": 5,
      "sentThisWeek": 8,
      "acceptedThisWeek": 3
    },
    "chatSummary": {
      "activeRooms": 15,
      "totalUnreadMessages": 42
    },
    "updatedAt": "2026-03-21T14:30:00Z"
  }
}
```

---

### 11.2 오늘의 일정 현황

> 관련 요구사항: ADM-101

#### `GET /api/v1/dashboard/today-schedule`

**설명**: 오늘의 전체 일정 항목 현황 (타임라인 형태)

**인증**: Bearer JWT
**권한**: ROLE_ADMIN

**Query Parameters**:
| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|------|--------|------|
| date | String | 아니오 | 오늘 | 특정 날짜 (YYYY-MM-DD) |

**Response** (`200 OK`):
```json
{
  "success": true,
  "message": "조회 성공",
  "data": {
    "date": "2026-04-16",
    "items": [
      {
        "scheduleItemId": 505,
        "journeyId": 100,
        "patientName": "John Doe",
        "title": "병원 진료",
        "type": "MEDICAL",
        "scheduledAt": "2026-04-16T10:00:00+09:00",
        "status": "IN_PROGRESS",
        "assignedStaff": [
          {"staffId": 11, "name": "이통역", "type": "INTERPRETER"}
        ]
      }
    ]
  }
}
```

---

## 12. File API

### 12.1 파일 업로드

> 관련 요구사항: PAT-201, PAT-502, STA-402

#### `POST /api/v1/files/upload`

**설명**: 범용 파일 업로드 (S3/MinIO). 업로드 후 파일 ID를 반환하여 다른 API에서 참조.

**인증**: Bearer JWT
**권한**: ROLE_ADMIN, ROLE_PATIENT, ROLE_STAFF

**Request Body** (`multipart/form-data`):
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| file | File | 예 | 업로드 파일 (최대 20MB) |
| category | String | 예 | `PASSPORT`, `MEDICAL_PHOTO`, `CHAT_FILE`, `PROOF_PHOTO`, `DOCUMENT` |

**Response** (`201 Created`):
```json
{
  "success": true,
  "message": "업로드 완료",
  "data": {
    "id": 123,
    "fileName": "passport_scan.jpg",
    "fileSize": 1024000,
    "mimeType": "image/jpeg",
    "category": "PASSPORT",
    "url": "https://cdn.example.com/files/123/passport_scan.jpg",
    "uploadedAt": "2026-03-21T14:30:00Z"
  }
}
```

**Error Cases**:
| HTTP Status | Error Code | 설명 |
|-------------|-----------|------|
| 400 | FIL_001 | 파일 크기 초과 (20MB) |
| 400 | FIL_002 | 지원하지 않는 파일 형식 |
| 400 | FIL_003 | 유효하지 않은 카테고리 |

---

### 12.2 파일 다운로드 (Presigned URL)

> 관련 요구사항: PAT-502, STA-402

#### `GET /api/v1/files/{fileId}/download`

**설명**: 파일의 보안 다운로드 URL 생성 (Presigned URL, 유효시간 제한)

**인증**: Bearer JWT
**권한**: ROLE_ADMIN, ROLE_PATIENT (본인 파일 또는 공유된 파일), ROLE_STAFF (배정된 여정 관련 파일)

**Path Parameters**:
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| fileId | Long | 예 | 파일 ID |

**Response** (`200 OK`):
```json
{
  "success": true,
  "message": "다운로드 URL 생성 완료",
  "data": {
    "fileId": 123,
    "fileName": "passport_scan.jpg",
    "downloadUrl": "https://s3.amazonaws.com/bucket/files/123?X-Amz-Signature=...",
    "expiresAt": "2026-03-21T15:30:00Z"
  }
}
```

**Error Cases**:
| HTTP Status | Error Code | 설명 |
|-------------|-----------|------|
| 403 | FIL_010 | 파일 접근 권한 없음 |
| 404 | FIL_011 | 존재하지 않는 파일 |

---

### 12.3 파일 삭제

> 관련 요구사항: PAT-201

#### `DELETE /api/v1/files/{fileId}`

**설명**: 업로드된 파일 삭제

**인증**: Bearer JWT
**권한**: ROLE_ADMIN, ROLE_PATIENT (본인 파일만)

**Path Parameters**:
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| fileId | Long | 예 | 파일 ID |

**Response** (`204 No Content`)

**Error Cases**:
| HTTP Status | Error Code | 설명 |
|-------------|-----------|------|
| 403 | FIL_020 | 타인의 파일 삭제 시도 |
| 404 | FIL_021 | 존재하지 않는 파일 |

---

## 13. Profile API

### 13.1 에이전시/병원 라이선스 검증 (환자)

> 관련 요구사항: PAT-301

#### `GET /api/v1/profiles/organizations/{organizationId}/license`

**설명**: 환자가 에이전시/병원의 라이선스 정보 및 검증 상태를 확인

**인증**: Bearer JWT
**권한**: ROLE_PATIENT, ROLE_ADMIN

**Path Parameters**:
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| organizationId | Long | 예 | 조직 ID |

**Response** (`200 OK`):
```json
{
  "success": true,
  "message": "조회 성공",
  "data": {
    "organizationId": 1,
    "type": "HOSPITAL",
    "name": "서울 뷰티 클리닉",
    "licenseNumber": "H-2025-001234",
    "licenseVerified": true,
    "verifiedAt": "2026-01-15T09:00:00Z",
    "licenseType": "의료기관 개설 허가증",
    "issuingAuthority": "서울특별시 강남구청",
    "validUntil": "2027-12-31",
    "specialties": ["성형외과", "피부과"],
    "address": "서울시 강남구 ...",
    "contactPhone": "+82-2-1234-5678"
  }
}
```

**Error Cases**:
| HTTP Status | Error Code | 설명 |
|-------------|-----------|------|
| 404 | PRF_001 | 존재하지 않는 조직 |

---

### 13.2 병원 포트폴리오 조회 (Before & After)

> 관련 요구사항: PAT-302

#### `GET /api/v1/profiles/organizations/{organizationId}/portfolio`

**설명**: 병원의 시술 포트폴리오(Before & After 사진) 조회

**인증**: Bearer JWT
**권한**: ROLE_PATIENT, ROLE_ADMIN

**Path Parameters**:
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| organizationId | Long | 예 | 병원 ID |

**Query Parameters**:
| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|------|--------|------|
| procedureType | String | 아니오 | 전체 | 시술 종류 필터 |
| page | int | 아니오 | 0 | 페이지 번호 |
| size | int | 아니오 | 20 | 페이지 크기 |

**Response** (`200 OK`):
```json
{
  "success": true,
  "message": "조회 성공",
  "data": {
    "content": [
      {
        "id": 1,
        "procedureType": "RHINOPLASTY",
        "title": "자연스러운 코 라인 교정",
        "description": "비절개 코 성형 - 30대 여성",
        "beforeImageUrl": "https://cdn.example.com/portfolio/1_before.jpg",
        "afterImageUrl": "https://cdn.example.com/portfolio/1_after.jpg",
        "surgeon": "Dr. Kim",
        "publishedAt": "2026-02-01T09:00:00Z"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 25,
    "totalPages": 2
  }
}
```

---

## 14. WebSocket 이벤트 명세

### 14.1 연결 정보

| 항목 | 값 |
|------|------|
| 프로토콜 | STOMP over WebSocket |
| 엔드포인트 | `wss://{domain}/ws` |
| 인증 | 연결 시 `Authorization` 헤더에 Bearer JWT 포함 |
| Heartbeat | 10초 간격 |
| 재연결 | 클라이언트 측 지수 백오프 (1s, 2s, 4s, 8s, max 30s) |

### 14.2 구독 토픽 (Subscribe)

#### 14.2.1 여정 상태 실시간 동기화

> 관련 요구사항: PAT-602, ADM-402, STA-401

**토픽**: `/topic/journey/{journeyId}/status`

**설명**: 여정 내 일정 항목의 상태 변경이 실시간으로 전달됨. 실무자 상태 변경 시 5초 이내 반영.

**수신 메시지 포맷**:
```json
{
  "eventType": "SCHEDULE_STATUS_CHANGED",
  "journeyId": 100,
  "scheduleItemId": 505,
  "previousStatus": "ARRIVED",
  "newStatus": "IN_PROGRESS",
  "updatedBy": {
    "id": 10,
    "name": "김기사",
    "role": "STAFF"
  },
  "note": "환자 픽업 완료, 병원으로 이동 중",
  "timestamp": "2026-04-16T10:15:00+09:00"
}
```

**구독 권한**: ROLE_ADMIN, ROLE_PATIENT (본인 여정), ROLE_STAFF (배정된 여정)

---

#### 14.2.2 여정 일정 변경 알림

> 관련 요구사항: PAT-603, ADM-402

**토픽**: `/topic/journey/{journeyId}/schedule`

**설명**: 관리자가 일정을 추가/수정/삭제했을 때 실시간 알림

**수신 메시지 포맷**:
```json
{
  "eventType": "SCHEDULE_UPDATED",
  "journeyId": 100,
  "scheduleItemId": 505,
  "action": "MODIFIED",
  "changes": {
    "scheduledAt": {
      "before": "2026-04-16T10:00:00+09:00",
      "after": "2026-04-16T11:00:00+09:00"
    },
    "title": {
      "before": "병원 진료",
      "after": "병원 진료 (시간 변경)"
    }
  },
  "reason": "병원 사정으로 1시간 지연",
  "timestamp": "2026-03-21T16:00:00Z"
}
```

> `action`: `ADDED`, `MODIFIED`, `DELETED`

---

#### 14.2.3 채팅 메시지 실시간 수신

> 관련 요구사항: PAT-501, ADM-501, ADM-502, STA-601

**토픽**: `/topic/chat/{roomId}`

**설명**: 채팅방의 새 메시지를 실시간 수신 (자동 번역 포함)

**수신 메시지 포맷**:
```json
{
  "eventType": "NEW_MESSAGE",
  "roomId": "room-100-pat5",
  "message": {
    "id": "msg-003",
    "senderId": 5,
    "senderName": "John Doe",
    "senderRole": "PATIENT",
    "type": "TEXT",
    "content": "What time is my appointment?",
    "translatedContent": {
      "ko": "진료 시간이 언제인가요?"
    },
    "sentAt": "2026-03-21T14:30:00Z"
  }
}
```

---

#### 14.2.4 채팅 읽음 상태 동기화

> 관련 요구사항: PAT-501, ADM-501

**토픽**: `/topic/chat/{roomId}/read`

**수신 메시지 포맷**:
```json
{
  "eventType": "MESSAGE_READ",
  "roomId": "room-100-pat5",
  "userId": 1,
  "lastReadMessageId": "msg-003",
  "timestamp": "2026-03-21T14:31:00Z"
}
```

---

#### 14.2.5 관리자 긴급 알림

> 관련 요구사항: ADM-102

**토픽**: `/topic/admin/alerts`

**설명**: 관리자 대시보드에 SOS, 긴급 상황, 시스템 알림을 실시간 전달

**수신 메시지 포맷**:
```json
{
  "eventType": "ALERT",
  "alert": {
    "id": 500,
    "type": "SOS",
    "urgencyLevel": "CRITICAL",
    "title": "긴급 SOS - 김기사",
    "body": "환자 상태 급변, 응급실 이송 필요",
    "source": {
      "staffId": 10,
      "staffName": "김기사",
      "journeyId": 100,
      "patientName": "John Doe"
    },
    "location": {
      "latitude": 37.5665,
      "longitude": 126.9780
    }
  },
  "timestamp": "2026-04-16T14:30:00+09:00"
}
```

**구독 권한**: ROLE_ADMIN

---

#### 14.2.6 관리자 대시보드 실시간 업데이트

> 관련 요구사항: ADM-101

**토픽**: `/topic/admin/dashboard`

**설명**: 대시보드 요약 정보가 변경될 때 실시간 업데이트

**수신 메시지 포맷**:
```json
{
  "eventType": "DASHBOARD_UPDATE",
  "section": "todaySummary",
  "data": {
    "completedItems": 21,
    "inProgressItems": 7
  },
  "timestamp": "2026-04-16T10:15:00+09:00"
}
```

---

#### 14.2.7 실무자 배정 알림

> 관련 요구사항: STA-202

**토픽**: `/topic/staff/{staffId}/assignments`

**설명**: 실무자에게 새 배정이 발생했을 때 실시간 알림

**수신 메시지 포맷**:
```json
{
  "eventType": "NEW_ASSIGNMENT",
  "assignment": {
    "id": 201,
    "journeyId": 100,
    "scheduleItemId": 506,
    "patientName": "John Doe",
    "title": "호텔 복귀 이동",
    "scheduledAt": "2026-04-16T14:00:00+09:00",
    "location": {
      "name": "강남 리커버리 호텔",
      "googleMapsUrl": "https://maps.google.com/?q=37.5172,127.0473"
    }
  },
  "timestamp": "2026-03-21T15:00:00Z"
}
```

**구독 권한**: ROLE_STAFF (본인 토픽만)

---

### 14.3 발행 (Publish / Send)

#### 14.3.1 일정 상태 업데이트 발행

**목적지**: `/app/journey/{journeyId}/status`

**발행 메시지 포맷**:
```json
{
  "scheduleItemId": 505,
  "status": "IN_PROGRESS",
  "note": "환자 픽업 완료, 병원으로 이동 중"
}
```

**발행 권한**: ROLE_STAFF (배정된 여정만)

---

#### 14.3.2 채팅 메시지 발행

**목적지**: `/app/chat/{roomId}/send`

**발행 메시지 포맷**:
```json
{
  "type": "TEXT",
  "content": "내일 진료 시간이 언제인가요?",
  "language": "ko"
}
```

**발행 권한**: 해당 채팅방 참여자

---

#### 14.3.3 실무자 위치 업데이트 발행

**목적지**: `/app/staff/location`

**발행 메시지 포맷**:
```json
{
  "latitude": 37.5665,
  "longitude": 126.9780
}
```

**발행 권한**: ROLE_STAFF

---

#### 14.3.4 채팅 타이핑 상태 발행

**목적지**: `/app/chat/{roomId}/typing`

**발행 메시지 포맷**:
```json
{
  "isTyping": true
}
```

**수신 토픽**: `/topic/chat/{roomId}/typing`

**수신 메시지 포맷**:
```json
{
  "userId": 5,
  "userName": "John Doe",
  "isTyping": true,
  "timestamp": "2026-03-21T14:30:00Z"
}
```

---

## 15. 요구사항-API 매핑 테이블

### 15.1 관리자 (Admin) 요구사항 매핑

| Req ID | 기능 | 우선순위 | 필수 | 매핑된 API 엔드포인트 |
|--------|------|---------|------|---------------------|
| ADM-101 | 운영 현황 요약 (Overview) | P1 | 필수 | `GET /api/v1/dashboard/overview`, `GET /api/v1/dashboard/today-schedule`, `WS /topic/admin/dashboard` |
| ADM-102 | 긴급 상황 알림 (Alert Center) | P1 | 필수 | `GET /api/v1/notifications/alerts`, `PATCH /api/v1/notifications/alerts/{alertId}/status`, `WS /topic/admin/alerts` |
| ADM-201 | 환자 DB 및 서류 검토 | P1 | 필수 | `GET /api/v1/patients`, `GET /api/v1/patients/{patientId}/passport`, `GET /api/v1/patients/{patientId}/medical-questionnaire`, `PATCH /api/v1/patients/{patientId}/verification` |
| ADM-301 | 스마트 견적서(Proposal) 생성 | P1 | 필수 | `POST /api/v1/proposals`, `GET /api/v1/proposals`, `GET /api/v1/proposals/{proposalId}`, `PUT /api/v1/proposals/{proposalId}`, `POST /api/v1/proposals/{proposalId}/send` |
| ADM-302 | 에이전시/병원 프로필 관리 | P2 | 필수 | `GET /api/v1/members/organizations`, `POST /api/v1/members/organizations`, `PUT /api/v1/members/organizations/{organizationId}` |
| ADM-401 | 여정 템플릿 빌더 (CMS) | P1 | 필수 | `GET /api/v1/journeys/templates`, `POST /api/v1/journeys/templates`, `GET /api/v1/journeys/templates/{templateId}`, `PUT /api/v1/journeys/templates/{templateId}`, `DELETE /api/v1/journeys/templates/{templateId}`, `POST /api/v1/journeys` |
| ADM-402 | 실시간 일정 수정 및 동기화 | P1 | 필수 | `GET /api/v1/journeys`, `GET /api/v1/journeys/{journeyId}`, `PUT /api/v1/journeys/{journeyId}/schedule-items/{itemId}`, `POST /api/v1/journeys/{journeyId}/schedule-items`, `DELETE /api/v1/journeys/{journeyId}/schedule-items/{itemId}`, `WS /topic/journey/{journeyId}/status`, `WS /topic/journey/{journeyId}/schedule` |
| ADM-501 | 통합 채팅 관제 (Multi-Chat) | P1 | 필수 | `POST /api/v1/chat/rooms`, `GET /api/v1/chat/rooms`, `GET /api/v1/chat/rooms/{roomId}/messages`, `GET /api/v1/chat/admin/monitor`, `WS /topic/chat/{roomId}` |
| ADM-502 | 자동 번역 지원 대화창 | P1 | 필수 | `POST /api/v1/chat/rooms/{roomId}/messages` (자동 번역 포함), `WS /topic/chat/{roomId}` (번역 결과 포함) |
| ADM-601 | 실무자 자동 배정 | P1 | 필수 | `GET /api/v1/staff`, `POST /api/v1/staff/auto-assign`, `POST /api/v1/staff/{staffId}/assign` |
| ADM-602 | 실무자 실시간 위치/상태 모니터링 | P2 | 옵션 | `GET /api/v1/staff/realtime`, `PUT /api/v1/staff/me/location` |
| ADM-701 | 사후 관리 가이드 배포 | P1 | 필수 | `POST /api/v1/aftercare/guides`, `GET /api/v1/aftercare/guides/{guideId}` |
| ADM-702 | 정산 및 인보이스 발행 | P2 | 필수 | `POST /api/v1/aftercare/invoices`, `GET /api/v1/aftercare/invoices` |
| ADM-801 | 권한 관리 (RBAC) | P1 | 필수 | `GET /api/v1/auth/roles`, `PUT /api/v1/auth/users/{userId}/role` |

### 15.2 환자 (Patient) 요구사항 매핑

| Req ID | 기능 | 우선순위 | 필수 | 매핑된 API 엔드포인트 |
|--------|------|---------|------|---------------------|
| PAT-101 | 글로벌 소셜 로그인 (OAuth) | P1 | 필수 | `POST /api/v1/auth/oauth/{provider}`, `POST /api/v1/auth/refresh`, `POST /api/v1/auth/logout` |
| PAT-102 | 글로벌 규제 동의 (T&C, Privacy) | P1 | 필수 | `POST /api/v1/auth/consent`, `GET /api/v1/auth/consent` |
| PAT-103 | 비회원 매직 링크 (Guest View) | P1 | 옵션 | `POST /api/v1/auth/magic-link`, `POST /api/v1/auth/magic-link/verify` |
| PAT-201 | 여권 정보 업로드 (Manual/OCR) | P1 | 필수 | `POST /api/v1/patients/me/passport`, `GET /api/v1/patients/{patientId}/passport`, `POST /api/v1/files/upload` |
| PAT-202 | 영문 의료/알레르기 문진표 | P1 | 필수 | `POST /api/v1/patients/me/medical-questionnaire`, `GET /api/v1/patients/{patientId}/medical-questionnaire`, `PUT /api/v1/patients/me/medical-questionnaire` |
| PAT-203 | 긴급 연락처 등록 | P1 | 필수 | `POST /api/v1/patients/me/emergency-contacts`, `GET /api/v1/patients/me/emergency-contacts`, `PUT /api/v1/patients/me/emergency-contacts/{contactId}`, `DELETE /api/v1/patients/me/emergency-contacts/{contactId}` |
| PAT-301 | 에이전시/병원 라이선스 검증 | P1 | 필수 | `GET /api/v1/profiles/organizations/{organizationId}/license` |
| PAT-302 | 병원 포트폴리오 (Before & After) | P2 | 옵션 | `GET /api/v1/profiles/organizations/{organizationId}/portfolio` |
| PAT-303 | 배정된 실무자 프로필 | P1 | 필수 | `GET /api/v1/staff/assigned` |
| PAT-401 | 수술/컨시어지 견적 요청 | P1 | 필수 | `POST /api/v1/proposals/request` |
| PAT-402 | 견적서 수신 및 비교 | P1 | 필수 | `GET /api/v1/proposals/me`, `GET /api/v1/proposals/{proposalId}` |
| PAT-403 | 견적 수락 및 기승인 | P1 | 필수 | `POST /api/v1/proposals/{proposalId}/respond` |
| PAT-501 | 에이전시 1:1 양문 텍스트 채팅 | P1 | 필수 | `GET /api/v1/chat/rooms`, `GET /api/v1/chat/rooms/{roomId}/messages`, `POST /api/v1/chat/rooms/{roomId}/messages`, `WS /topic/chat/{roomId}` |
| PAT-502 | 의료 사진 및 문서 보안 전송 | P1 | 필수 | `POST /api/v1/chat/rooms/{roomId}/messages/file`, `POST /api/v1/files/upload` |
| PAT-503 | 시스템 알림 메시지 | P1 | 필수 | `GET /api/v1/notifications`, `PATCH /api/v1/notifications/{notificationId}/read`, `PATCH /api/v1/notifications/read-all` |
| PAT-601 | 라이브 타임라인 (Live Itinerary) | P1 | 필수 | `GET /api/v1/journeys/me/timeline`, `GET /api/v1/journeys/{journeyId}` |
| PAT-602 | 일정 상태 실시간 동기화 | P1 | 필수 | `WS /topic/journey/{journeyId}/status` |
| PAT-603 | 일정 변동 시 푸시 알림 | P1 | 필수 | `POST /api/v1/notifications/devices`, `WS /topic/journey/{journeyId}/schedule` |
| PAT-604 | Google Maps 딥링크 연동 | P2 | 필수 | `GET /api/v1/journeys/me/timeline` (googleMapsUrl 필드 포함) |
| PAT-701 | 수술 후 사후 관리 가이드 | P1 | 필수 | `GET /api/v1/aftercare/guides/me`, `GET /api/v1/aftercare/guides/{guideId}` |
| PAT-702 | 영수증 및 인보이스 내역 | P2 | 필수 | `GET /api/v1/aftercare/invoices/me` |

### 15.3 실무자 (Staff) 요구사항 매핑

| Req ID | 기능 | 우선순위 | 필수 | 매핑된 API 엔드포인트 |
|--------|------|---------|------|---------------------|
| STA-101 | 간편 로그인 (Magic Link/SNS) | P1 | 필수 | `POST /api/v1/auth/magic-link`, `POST /api/v1/auth/magic-link/verify`, `POST /api/v1/auth/oauth/{provider}` |
| STA-102 | 실무자 프로필 관리 | P1 | 필수 | `GET /api/v1/members/staff/me`, `PUT /api/v1/members/staff/me` |
| STA-201 | 당일 업무 리스트 (To-do) | P1 | 필수 | `GET /api/v1/journeys/staff/me/today` |
| STA-202 | 배정 알림 푸시 (Assignment) | P1 | 필수 | `POST /api/v1/notifications/devices`, `GET /api/v1/notifications`, `WS /topic/staff/{staffId}/assignments` |
| STA-301 | 환자 특이사항 조회 (Notice) | P1 | 필수 | `GET /api/v1/journeys/{journeyId}/patient-notice` |
| STA-401 | 원터치 상태 업데이트 (Status) | P1 | 필수 | `PATCH /api/v1/journeys/{journeyId}/schedule-items/{itemId}/status`, `WS /app/journey/{journeyId}/status` |
| STA-402 | 현장 사진 업로드 (Proof) | P2 | 필수 | `POST /api/v1/journeys/{journeyId}/schedule-items/{itemId}/photos`, `POST /api/v1/files/upload` |
| STA-501 | 지도 앱 딥링크 연동 | P1 | 필수 | `GET /api/v1/journeys/{journeyId}/schedule-items/{itemId}/navigation` |
| STA-601 | 에이전시/환자 1:1 채널 | P1 | 필수 | `GET /api/v1/chat/rooms`, `GET /api/v1/chat/rooms/{roomId}/messages`, `POST /api/v1/chat/rooms/{roomId}/messages`, `WS /topic/chat/{roomId}` |
| STA-602 | 긴급 호출 (SOS) | P2 | 옵션 | `POST /api/v1/chat/sos` |
| STA-701 | 업무 종료 리포트 | P2 | 필수 | `POST /api/v1/aftercare/staff-reports` |

### 15.4 커버리지 요약

| 구분 | 전체 | P1 필수 | P1 옵션 | P2 필수 | P2 옵션 | API 매핑 완료 |
|------|------|---------|---------|---------|---------|-------------|
| 관리자 (ADM) | 14 | 10 | 0 | 3 | 1 | 14/14 (100%) |
| 환자 (PAT) | 18 | 14 | 1 | 2 | 1 | 18/18 (100%) |
| 실무자 (STA) | 11 | 7 | 0 | 3 | 1 | 11/11 (100%) |
| **합계** | **43** | **31** | **1** | **8** | **3** | **43/43 (100%)** |
