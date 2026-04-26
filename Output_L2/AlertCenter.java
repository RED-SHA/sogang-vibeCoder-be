// Boundary: 알림 센터
// Source: UseCaseDescription_UC-ADM-07_reviewd.md
//   - A4.1: 재시도가 모두 소진되면 시스템은 실패를 알림 센터에 기록한다.
import java.util.List;

public interface AlertCenter {

    // A4.1: record push notification failure after retries exhausted
    void recordFailure(List<String> staffIds, String patientMagicLinkEndpoint);
}
