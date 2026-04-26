// Abstract Use Case: ABS-04 "Synchronize Realtime State"
// Package: Cross-Cutting (Abstract)
// External Dependency: Realtime Sync Bus (EXT_SYNC)
// Included by (subset): UC-ADM-07 Edit Itinerary
// Source: UseCaseDiagram.puml lines 111, 199, 249
public abstract class SynchronizeRealtimeState {

    protected final RealtimeSyncBus realtimeSyncBus;

    protected SynchronizeRealtimeState(RealtimeSyncBus realtimeSyncBus) {
        this.realtimeSyncBus = realtimeSyncBus;
    }

    // Common flow entry. Diagram does not define internal steps.
    public void run() {
        // TODO: synchronization steps not specified in Use Case Diagram
    }
}
