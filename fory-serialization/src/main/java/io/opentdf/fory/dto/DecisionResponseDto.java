package io.opentdf.fory.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Response containing authorization decision result.
 */
public class DecisionResponseDto {
    /**
     * Decision enumeration.
     */
    public enum Decision {
        UNSPECIFIED,
        DENY,
        PERMIT
    }

    /**
     * Ephemeral entity chain id from the request.
     */
    private String entityChainId;

    /**
     * Ephemeral resource attributes id from the request.
     */
    private String resourceAttributesId;

    /**
     * Action of the decision response.
     */
    private ActionDto action;

    /**
     * The decision response.
     */
    private Decision decision;

    /**
     * Optional list of obligations represented in URI format.
     */
    private List<String> obligations;

    public DecisionResponseDto() {
        this.obligations = new ArrayList<>();
    }

    public String getEntityChainId() {
        return entityChainId;
    }

    public void setEntityChainId(String entityChainId) {
        this.entityChainId = entityChainId;
    }

    public String getResourceAttributesId() {
        return resourceAttributesId;
    }

    public void setResourceAttributesId(String resourceAttributesId) {
        this.resourceAttributesId = resourceAttributesId;
    }

    public ActionDto getAction() {
        return action;
    }

    public void setAction(ActionDto action) {
        this.action = action;
    }

    public Decision getDecision() {
        return decision;
    }

    public void setDecision(Decision decision) {
        this.decision = decision;
    }

    public List<String> getObligations() {
        return obligations;
    }

    public void setObligations(List<String> obligations) {
        this.obligations = obligations;
    }

    public boolean isPermit() {
        return decision == Decision.PERMIT;
    }

    public boolean isDeny() {
        return decision == Decision.DENY;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DecisionResponseDto that = (DecisionResponseDto) o;
        return Objects.equals(entityChainId, that.entityChainId) &&
               Objects.equals(resourceAttributesId, that.resourceAttributesId) &&
               Objects.equals(action, that.action) &&
               decision == that.decision &&
               Objects.equals(obligations, that.obligations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityChainId, resourceAttributesId, action, decision, obligations);
    }

    @Override
    public String toString() {
        return "DecisionResponseDto{entityChainId='" + entityChainId +
               "', resourceAttributesId='" + resourceAttributesId +
               "', decision=" + decision + ", obligations=" + obligations + "}";
    }
}
