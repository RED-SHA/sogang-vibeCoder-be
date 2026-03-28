# Sprint 6 Contract: Chat + Translation

## 범위

### 요구사항
- ADM-501: 통합 채팅 관제 Multi-Chat
- ADM-502: 자동 번역 지원
- PAT-501: 에이전시 1:1 채팅
- PAT-502: 의료 사진/문서 보안 전송
- PAT-503: 시스템 알림 메시지
- STA-601: 에이전시/환자 1:1 채널
- STA-602: 긴급 호출 (SOS)

### 구현할 엔드포인트 (8개)
- POST /api/v1/chat/rooms — 채팅방 생성
- GET /api/v1/chat/rooms — 내 채팅방 목록
- GET /api/v1/chat/rooms/{roomId}/messages — 메시지 이력 (커서 기반)
- POST /api/v1/chat/rooms/{roomId}/messages — 텍스트 메시지 전송
- POST /api/v1/chat/rooms/{roomId}/messages/file — 파일 메시지 전송
- POST /api/v1/chat/rooms/{roomId}/read — 읽음 처리
- GET /api/v1/chat/admin/monitor — 관리자 멀티챗 관제
- POST /api/v1/chat/sos — 긴급 호출

### Entity
- ChatRoom, ChatRoomParticipant, ChatMessage

### Enum
- ChatRoomType (PATIENT_AGENCY, STAFF_AGENCY, PATIENT_STAFF)
- ChatMessageType (TEXT, FILE, SYSTEM, SOS)

## 수락 기준
- [ ] compileJava + compileTestJava 성공
- [ ] ChatService 단위 테스트, ChatController 슬라이스 테스트
- [ ] 채팅방 참여자 검증
- [ ] 번역: MVP에서는 translatedContent를 빈 Map으로 반환 (TODO: 번역 API 연동)
- [ ] SOS: 긴급 호출 메시지 생성
- [ ] Flyway V7

## 예상 파일 수: ~25개
## 의존성: Sprint 1, Sprint 3
