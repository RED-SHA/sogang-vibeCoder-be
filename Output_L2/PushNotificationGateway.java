// External Actor / Boundary: Push Notification Gateway
// Source: UseCaseDescription_UC-ADM-07_reviewd.md — used by Send Push Notification (Step 12)
import java.util.List;

public interface PushNotificationGateway {

    // Single dispatch attempt — returns true on success, false on 5xx (A4 trigger)
    // TODO: 5xx detection / response protocol not specified in Description
    boolean dispatch(List<String> staffIds, String patientMagicLinkEndpoint);
}
