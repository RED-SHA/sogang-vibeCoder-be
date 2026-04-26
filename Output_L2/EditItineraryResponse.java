// Use case response (immutable)
// Source: UseCaseDescription_UC-ADM-07_reviewd.md
//   - Step 14: 갱신된 일정 뷰
//   - A3.2: degraded_sync 플래그 (boolean field, 매직 스트링 금지)
//   - A1/A2/A6/A7: error path → ErrorCode enum
public final class EditItineraryResponse {

    private final boolean success;
    private final Object updatedItineraryView;
    private final boolean degradedSync;
    private final ErrorCode errorCode;
    private final String errorDetail;

    private EditItineraryResponse(boolean success,
                                  Object updatedItineraryView,
                                  boolean degradedSync,
                                  ErrorCode errorCode,
                                  String errorDetail) {
        this.success = success;
        this.updatedItineraryView = updatedItineraryView;
        this.degradedSync = degradedSync;
        this.errorCode = errorCode;
        this.errorDetail = errorDetail;
    }

    public static EditItineraryResponse success(Object updatedItineraryView, boolean degradedSync) {
        return new EditItineraryResponse(true, updatedItineraryView, degradedSync, null, null);
    }

    public static EditItineraryResponse error(ErrorCode errorCode, String errorDetail) {
        return new EditItineraryResponse(false, null, false, errorCode, errorDetail);
    }

    public boolean isSuccess() { return success; }
    public Object getUpdatedItineraryView() { return updatedItineraryView; }
    public boolean isDegradedSync() { return degradedSync; }
    public ErrorCode getErrorCode() { return errorCode; }
    public String getErrorDetail() { return errorDetail; }
}
