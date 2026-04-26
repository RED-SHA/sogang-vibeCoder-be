// Abstract Use Case: Audit Log Action (Dependency)
// Triggered at: Description Step 10, A6.2
// Inputs (Step 10): operator_id, before/after diff (§2 JSON Patch), ISO 8601 timestamp
// Inputs (A6.2): operator_id, rejection reason
// Source: UseCaseDescription_UC-ADM-07_reviewd.md — Dependency, §4 trace, §2
import java.time.Instant;
import java.util.List;

public class AuditLogAction {

    private final AuditLogRepository auditLogRepository;

    public AuditLogAction(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    // Step 10: include with operator_id, diff, ISO 8601 timestamp
    public void run(String operatorId, List<JsonPatchEntry> diff, Instant timestamp) {
        // TODO: persistence call shape not specified beyond "감사 로그 저장소에 영속화"
        auditLogRepository.persist(operatorId, diff, timestamp, null);
    }

    // A6.2: include with operator_id and rejection reason
    public void runRejection(String operatorId, String rejectionReason, Instant timestamp) {
        // TODO: persistence call shape not specified beyond "감사 로그 저장소에 영속화"
        auditLogRepository.persist(operatorId, null, timestamp, rejectionReason);
    }
}
