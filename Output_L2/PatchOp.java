// JSON Patch operation enum — replaces string literals
// Source: UseCaseDescription_UC-ADM-07_reviewd.md — 부록 §2
//   op ∈ {replace, add, remove}
public enum PatchOp {
    REPLACE,
    ADD,
    REMOVE
}
