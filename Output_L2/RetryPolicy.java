// Retry Policy constants
// Source: UseCaseDescription_UC-ADM-07_reviewd.md — 부록 §1
//   - 알고리즘: 지수 백오프
//   - 초기 지연: 1 초
//   - 배수: 2
//   - 최대 시도 횟수: 3 회 (1초 → 2초 → 4초)
// 적용: A3 실시간 동기화 발행 / A4 푸시 알림 게이트웨이 호출
public final class RetryPolicy {

    public static final long INITIAL_DELAY_SECONDS = 1L;
    public static final long BACKOFF_MULTIPLIER = 2L;
    public static final int MAX_ATTEMPTS = 3;

    private RetryPolicy() {
        // constants only
    }
}
