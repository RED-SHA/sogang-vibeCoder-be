// Boundary: 감사 로그 저장소
// Source: UseCaseDescription_UC-ADM-07_reviewd.md
//   - Postcondition 3: 운영자 식별자·변경 전후 diff·ISO 8601 타임스탬프 영속화
//   - 부록 §2: diff = JSON Patch entries
import java.time.Instant;
import java.util.List;

public interface AuditLogRepository {

    // Step 10: persist body-change audit (diff present)
    // A6.2: persist rejection audit (rejectionReason present, diff null)
    void persist(String operatorId,
                 List<JsonPatchEntry> diff,
                 Instant isoTimestamp,
                 String rejectionReason);
}
