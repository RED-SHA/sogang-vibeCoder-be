// JSON Patch entry value object (immutable)
// Source: UseCaseDescription_UC-ADM-07_reviewd.md — 부록 §2 (RFC 6902)
//   필드: op, path, value
public final class JsonPatchEntry {

    private final PatchOp op;
    private final String path;
    private final Object value;

    public JsonPatchEntry(PatchOp op, String path, Object value) {
        this.op = op;
        this.path = path;
        this.value = value;
    }

    public PatchOp getOp() { return op; }
    public String getPath() { return path; }
    public Object getValue() { return value; }
}
