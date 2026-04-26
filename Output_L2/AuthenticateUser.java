// Abstract Use Case: Authenticate User (Dependency)
// Triggered at: Description Step 1
// Source: UseCaseDescription_UC-ADM-07_reviewd.md — Dependency, §4 trace
public class AuthenticateUser {

    // Step 1: 시스템은 Authenticate User 결과를 확인한다.
    public AuthenticationResult run(String operatorId) {
        // TODO: authentication mechanism not specified in Description
        return null;
    }

    // Result carrier. Description Step 1 also implies RBAC write-permission check (A6).
    public static class AuthenticationResult {
        private final boolean authenticated;
        private final boolean writePermissionGranted;
        private final String denialReason;

        public AuthenticationResult(boolean authenticated,
                                    boolean writePermissionGranted,
                                    String denialReason) {
            this.authenticated = authenticated;
            this.writePermissionGranted = writePermissionGranted;
            this.denialReason = denialReason;
        }

        public boolean isAuthenticated() { return authenticated; }
        public boolean isWritePermissionGranted() { return writePermissionGranted; }
        public String getDenialReason() { return denialReason; }
    }
}
