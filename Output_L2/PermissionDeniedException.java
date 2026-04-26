// Exception: A6 — RBAC 검사가 대상 케이스에 대한 쓰기 권한을 거부
// Source: UseCaseDescription_UC-ADM-07_reviewd.md — Alternatives A6
public class PermissionDeniedException extends Exception {

    private final String reason;

    public PermissionDeniedException(String reason) {
        super("permission_denied");
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }
}
