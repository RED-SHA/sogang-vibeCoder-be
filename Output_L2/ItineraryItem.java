// Itinerary item (immutable, with version field — see Postcondition 1)
// Source: UseCaseDescription_UC-ADM-07_reviewd.md
//   - Step 8: version 필드를 1만큼 증가
//   - Step 9: 배정 실무자 연관 갱신
//   - A2: version 정수 비교 기반 낙관적 락
import java.time.Instant;
import java.util.Collections;
import java.util.List;

public final class ItineraryItem {

    private final String id;
    private final int version;
    private final Instant startTime;
    private final Instant endTime;
    private final String location;
    private final List<String> assignedStaffIds;

    public ItineraryItem(String id,
                         int version,
                         Instant startTime,
                         Instant endTime,
                         String location,
                         List<String> assignedStaffIds) {
        this.id = id;
        this.version = version;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.assignedStaffIds = assignedStaffIds == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(assignedStaffIds);
    }

    public String getId() { return id; }
    public int getVersion() { return version; }
    public Instant getStartTime() { return startTime; }
    public Instant getEndTime() { return endTime; }
    public String getLocation() { return location; }
    public List<String> getAssignedStaffIds() { return assignedStaffIds; }

    // Step 8: increment version by 1 → returns new instance (immutable)
    public ItineraryItem withIncrementedVersion() {
        return new ItineraryItem(id, version + 1, startTime, endTime, location, assignedStaffIds);
    }

    // Step 9: update assigned staff association → returns new instance
    public ItineraryItem withAssignedStaff(List<String> newAssignedStaffIds) {
        return new ItineraryItem(id, version, startTime, endTime, location, newAssignedStaffIds);
    }
}
