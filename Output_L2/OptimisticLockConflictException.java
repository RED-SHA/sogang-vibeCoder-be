// Exception: A2 — version 정수 비교 기반 낙관적 락 충돌
// Source: UseCaseDescription_UC-ADM-07_reviewd.md — Alternatives A2
public class OptimisticLockConflictException extends Exception {

    private final int expectedVersion;
    private final int actualVersion;

    public OptimisticLockConflictException(int expectedVersion, int actualVersion) {
        super("optimistic_lock_conflict");
        this.expectedVersion = expectedVersion;
        this.actualVersion = actualVersion;
    }

    public int getExpectedVersion() { return expectedVersion; }
    public int getActualVersion() { return actualVersion; }
}
