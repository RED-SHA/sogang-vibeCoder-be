// Actor: 에이전시 운영자 (Agency Operator)
// Source: UseCaseDescription_UC-ADM-07_reviewd.md — Actor
public class AgencyOperator {
    private final String operatorId;

    public AgencyOperator(String operatorId) {
        this.operatorId = operatorId;
    }

    public String getOperatorId() {
        return operatorId;
    }
    // TODO: additional operator attributes not specified in Description
}
