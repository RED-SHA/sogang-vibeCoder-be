// Exception: A7 — 락 보유자 식별자를 포함한 lock_held 오류
// Source: UseCaseDescription_UC-ADM-07_reviewd.md — Alternatives A7
public class LockHeldException extends Exception {

    private final String lockHolderId;

    public LockHeldException(String lockHolderId) {
        super("lock_held");
        this.lockHolderId = lockHolderId;
    }

    public String getLockHolderId() {
        return lockHolderId;
    }
}
