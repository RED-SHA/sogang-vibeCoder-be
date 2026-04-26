// Submitted modification fields (immutable)
// Source: UseCaseDescription_UC-ADM-07_reviewd.md — Description Step 5
//   "시작 시각, 종료 시각, 장소, 배정 실무자 식별자를 포함한 수정 필드를 제출한다"
import java.time.Instant;
import java.util.Collections;
import java.util.List;

public final class EditPayload {

    private final Instant startTime;
    private final Instant endTime;
    private final String location;
    private final List<String> assignedStaffIds;

    public EditPayload(Instant startTime,
                       Instant endTime,
                       String location,
                       List<String> assignedStaffIds) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.assignedStaffIds = assignedStaffIds == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(assignedStaffIds);
    }

    public Instant getStartTime() { return startTime; }
    public Instant getEndTime() { return endTime; }
    public String getLocation() { return location; }
    public List<String> getAssignedStaffIds() { return assignedStaffIds; }
}
