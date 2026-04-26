// Error code enum — replaces string literals
// Source: UseCaseDescription_UC-ADM-07_reviewd.md
//   - LOCK_HELD            : A7 (Step 3 lock acquisition failure, "lock_held" 오류)
//   - PERMISSION_DENIED    : A6.3 (Step 1 RBAC denial, 권한 오류 코드)
//   - VALIDATION_FAILED    : A1 (Step 6 validation failure, 필드 수준 오류 마커)
//   - OPTIMISTIC_LOCK_CONFLICT : A2 (Step 7 version conflict, 머지 뷰)
public enum ErrorCode {
    LOCK_HELD,
    PERMISSION_DENIED,
    VALIDATION_FAILED,
    OPTIMISTIC_LOCK_CONFLICT
}
