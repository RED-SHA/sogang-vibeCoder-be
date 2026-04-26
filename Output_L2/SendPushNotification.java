// Abstract Use Case: Send Push Notification (Dependency)
// Triggered at: Description Step 12
// Targets: 배정된 모든 실무자 + 환자 매직 링크 엔드포인트
// Failure path: A4 — retry per §1, then record failure to AlertCenter, continue
// Source: UseCaseDescription_UC-ADM-07_reviewd.md — Dependency, §4 trace, §1
import java.util.List;

public class SendPushNotification {

    private final PushNotificationGateway pushNotificationGateway;

    public SendPushNotification(PushNotificationGateway pushNotificationGateway) {
        this.pushNotificationGateway = pushNotificationGateway;
    }

    // Step 12: queue change notification to assigned staff & patient magic-link endpoints
    // Returns true on success, false if gateway returned 5xx for all retries (§1)
    public boolean queue(List<String> staffIds, String patientMagicLinkEndpoint) {
        int attempts = 0;
        long delaySeconds = RetryPolicy.INITIAL_DELAY_SECONDS;
        while (attempts < RetryPolicy.MAX_ATTEMPTS) {
            attempts++;
            boolean ok = tryQueueOnce(staffIds, patientMagicLinkEndpoint);
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

    private boolean tryQueueOnce(List<String> staffIds, String patientMagicLinkEndpoint) {
        // TODO: 5xx detection not specified beyond "푸시 알림 게이트웨이가 5xx 응답을 반환하면"
        return pushNotificationGateway.dispatch(staffIds, patientMagicLinkEndpoint);
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
