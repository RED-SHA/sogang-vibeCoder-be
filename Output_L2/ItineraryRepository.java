// Boundary: 일정 저장소
// Source: UseCaseDescription_UC-ADM-07_reviewd.md
//   - Step 3: 편집 락 획득 (A7 if held)
//   - Step 4: 편집 가능한 폼 반환
//   - Step 7: 새 리비전 기록 (A2 on optimistic lock conflict)
//   - Step 13: 편집 락 해제
public interface ItineraryRepository {

    // Step 3: acquire edit lock — throws LockHeldException for A7
    void acquireLock(String itineraryItemId) throws LockHeldException;

    // Step 4: return current schedule with editable form
    ItineraryItem loadEditableForm(String itineraryItemId);

    // Step 7: write new revision — throws OptimisticLockConflictException for A2
    ItineraryItem writeNewRevision(String itineraryItemId, EditPayload payload, int expectedVersion)
            throws OptimisticLockConflictException;

    // A2: reload latest revision for merge view
    ItineraryItem reloadLatest(String itineraryItemId);

    // Step 13 / A5.2: release edit lock
    void releaseLock(String itineraryItemId);
}
