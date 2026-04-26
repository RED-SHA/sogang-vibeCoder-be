// Exception: A1 — 스키마 오류·과거 시각·형제 항목 중복으로 검증 실패
// Source: UseCaseDescription_UC-ADM-07_reviewd.md — Alternatives A1
//   "필드 수준 오류 마커와 함께 제출을 거부한다"
import java.util.Collections;
import java.util.Map;

public class ValidationException extends Exception {

    private final Map<String, String> fieldErrors;

    public ValidationException(Map<String, String> fieldErrors) {
        super("validation_failed");
        this.fieldErrors = fieldErrors == null
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(fieldErrors);
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
}
