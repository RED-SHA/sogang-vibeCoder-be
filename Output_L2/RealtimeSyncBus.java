// External Actor / Boundary: Realtime Sync Bus
// Source: UseCaseDescription_UC-ADM-07_reviewd.md — used by Synchronize Realtime State (Step 11)
public interface RealtimeSyncBus {

    // Single publish attempt — returns true on success, false on failure
    // TODO: success/failure protocol not specified in Description
    boolean publish(ItineraryItem snapshot);
}
