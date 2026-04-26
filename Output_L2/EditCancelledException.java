// Exception: A5 — 4 단계와 7 단계 사이에서 운영자 편집 취소
// Source: UseCaseDescription_UC-ADM-07_reviewd.md — Alternatives A5
public class EditCancelledException extends Exception {

    public EditCancelledException() {
        super("edit_cancelled");
    }
}
