// Use Case: UC-ADM-07 "Edit Itinerary"
// Package: Agency Back-Office
// Primary Actor: Agency Admin (AA)
// Includes (always executed):
//   - ABS-04 Synchronize Realtime State
//   - ABS-05 Send Push Notification
//   - ABS-06 Audit Log Action
// Source: UseCaseDiagram.puml lines 53, 149, 199-201
public class EditItinerary {

    private final AgencyAdmin agencyAdmin;
    private final SynchronizeRealtimeState synchronizeRealtimeState; // ABS-04
    private final SendPushNotification sendPushNotification;         // ABS-05
    private final AuditLogAction auditLogAction;                     // ABS-06

    public EditItinerary(AgencyAdmin agencyAdmin,
                         SynchronizeRealtimeState synchronizeRealtimeState,
                         SendPushNotification sendPushNotification,
                         AuditLogAction auditLogAction) {
        this.agencyAdmin = agencyAdmin;
        this.synchronizeRealtimeState = synchronizeRealtimeState;
        this.sendPushNotification = sendPushNotification;
        this.auditLogAction = auditLogAction;
    }

    // Main flow entry. Diagram does not define internal steps or parameters.
    public void execute() {
        // TODO: main scenario steps not specified in Use Case Diagram

        // <<include>> ABS-04
        synchronizeRealtimeState.run();

        // <<include>> ABS-05
        sendPushNotification.run();

        // <<include>> ABS-06
        auditLogAction.run();
    }
}
