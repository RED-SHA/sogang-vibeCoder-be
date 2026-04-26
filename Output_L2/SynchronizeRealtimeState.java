// Abstract Use Case: Synchronize Realtime State (Dependency)
// Triggered at: Description Step 11
// Constraint: 5초 이내 전송 (NFR 실시간 동기화)
// Failure path: A3 — retry per §1, then record delta to DelayedDispatchQueue, set degraded_sync flag
// Source: UseCaseDescription_UC-ADM-07_reviewd.md — Dependency, §4 trace, §1
public class SynchronizeRealtimeState {

    private final RealtimeSyncBus realtimeSyncBus;

    public SynchronizeRealtimeState(RealtimeSyncBus realtimeSyncBus) {
        this.realtimeSyncBus = realtimeSyncBus;
    }

    // Step 11: publish updated schedule snapshot to subscribed patient & assigned-staff clients
    // Returns true on success, false if all retries (§1) exhaust → caller handles A3.1/A3.2
    public boolean publish(ItineraryItem snapshot) {
        int attempts = 0;
        long delaySeconds = RetryPolicy.INITIAL_DELAY_SECONDS;
        while (attempts < RetryPolicy.MAX_ATTEMPTS) {
            attempts++;
            boolean ok = tryPublishOnce(snapshot);
            if (ok) {
                return true;
            }
            if (attempts >= RetryPolicy.MAX_ATTEMPTS) {
                break;
            }
            sleepSeconds(delaySeconds);
            delaySeconds = delaySeconds * RetryPolicy.BACKOFF_MULTIPLIER;
        }
        return false;
    }

    private boolean tryPublishOnce(ItineraryItem snapshot) {
        // TODO: success/failure determination not specified in Description
        return realtimeSyncBus.publish(snapshot);
    }

    private void sleepSeconds(long seconds) {
        // TODO: blocking strategy not specified in Description
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
