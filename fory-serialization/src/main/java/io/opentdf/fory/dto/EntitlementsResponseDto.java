package io.opentdf.fory.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Response containing entity entitlements.
 */
public class EntitlementsResponseDto {
    /**
     * List of entity entitlements.
     */
    private List<EntityEntitlementsDto> entitlements;

    public EntitlementsResponseDto() {
        this.entitlements = new ArrayList<>();
    }

    public List<EntityEntitlementsDto> getEntitlements() {
        return entitlements;
    }

    public void setEntitlements(List<EntityEntitlementsDto> entitlements) {
        this.entitlements = entitlements;
    }

    public void addEntitlement(EntityEntitlementsDto entitlement) {
        if (this.entitlements == null) {
            this.entitlements = new ArrayList<>();
        }
        this.entitlements.add(entitlement);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntitlementsResponseDto that = (EntitlementsResponseDto) o;
        return Objects.equals(entitlements, that.entitlements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entitlements);
    }

    @Override
    public String toString() {
        return "EntitlementsResponseDto{entitlements=" + entitlements + "}";
    }

    /**
     * Entitlements for a single entity.
     */
    public static class EntityEntitlementsDto {
        /**
         * Entity identifier.
         */
        private String entityId;

        /**
         * List of attribute value FQNs the entity is entitled to.
         */
        private List<String> attributeValueFqns;

        public EntityEntitlementsDto() {
            this.attributeValueFqns = new ArrayList<>();
        }

        public EntityEntitlementsDto(String entityId, List<String> attributeValueFqns) {
            this.entityId = entityId;
            this.attributeValueFqns = attributeValueFqns != null ? attributeValueFqns : new ArrayList<>();
        }

        public String getEntityId() {
            return entityId;
        }

        public void setEntityId(String entityId) {
            this.entityId = entityId;
        }

        public List<String> getAttributeValueFqns() {
            return attributeValueFqns;
        }

        public void setAttributeValueFqns(List<String> attributeValueFqns) {
            this.attributeValueFqns = attributeValueFqns;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            EntityEntitlementsDto that = (EntityEntitlementsDto) o;
            return Objects.equals(entityId, that.entityId) &&
                   Objects.equals(attributeValueFqns, that.attributeValueFqns);
        }

        @Override
        public int hashCode() {
            return Objects.hash(entityId, attributeValueFqns);
        }

        @Override
        public String toString() {
            return "EntityEntitlementsDto{entityId='" + entityId +
                   "', attributeValueFqns=" + attributeValueFqns + "}";
        }
    }
}
