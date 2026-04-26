// Boundary: 지연 발송 큐
// Source: UseCaseDescription_UC-ADM-07_reviewd.md
//   - A3.1: 최종 실패 시 시스템은 지연 발송 큐에 delta 를 기록한다.
//   - Postcondition 4a: 동기화 실패 시 delta 항목 등록
public interface DelayedDispatchQueue {

    // A3.1: record delta when realtime sync publish ultimately fails
    void recordDelta(ItineraryItem snapshot);
}
