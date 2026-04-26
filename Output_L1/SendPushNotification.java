// Abstract Use Case: ABS-05 "Send Push Notification"
// Package: Cross-Cutting (Abstract)
// External Dependency: Push Notification Gateway (EXT_PUSH)
// Included by (subset): UC-ADM-07 Edit Itinerary
// Source: UseCaseDiagram.puml lines 112, 200, 250
public abstract class SendPushNotification {

    protected final PushNotificationGateway pushNotificationGateway;

    protected SendPushNotification(PushNotificationGateway pushNotificationGateway) {
        this.pushNotificationGateway = pushNotificationGateway;
    }

    // Common flow entry. Diagram does not define internal steps.
    public void run() {
        // TODO: push notification steps not specified in Use Case Diagram
    }
}
