package io.opentdf.fory.dto;

import org.apache.fory.annotation.ForyField;
import java.util.Objects;

/**
 * Person Entity (PE) or Non-Person Entity (NPE).
 *
 * Optimized for Fory serialization:
 * - Final class enables JIT devirtualization
 * - Enum values with fast ordinal lookup (Section 9)
 * - No defensive copies or unnecessary allocations (Section 5)
 */
public final class EntityDto {
    /**
     * Ephemeral id for tracking between request and response.
     */
    private String ephemeralId;

    /**
     * Entity type enumeration.
     */
    public enum EntityType {
        UNSPECIFIED,
        EMAIL_ADDRESS,
        USER_NAME,
        CLAIMS,
        CLIENT_ID;

        // Lookup table for fast ordinal-to-enum conversion (Section 9)
        private static final EntityType[] VALUES = values();

        public static EntityType fromOrdinal(int ordinal) {
            return (ordinal >= 0 && ordinal < VALUES.length) ? VALUES[ordinal] : UNSPECIFIED;
        }
    }

    /**
     * The type of entity.
     */
    private EntityType entityType;

    /**
     * The entity identifier value (email, username, client_id, etc.).
     */
    private String entityValue;

    /**
     * Claims data as JSON string (when entityType is CLAIMS).
     * Often null - Fory handles null efficiently.
     */
    @ForyField(nullable = true)
    private String claims;

    /**
     * Entity category enumeration.
     */
    public enum Category {
        UNSPECIFIED,
        SUBJECT,
        ENVIRONMENT;

        // Lookup table for fast ordinal-to-enum conversion (Section 9)
        private static final Category[] VALUES = values();

        public static Category fromOrdinal(int ordinal) {
            return (ordinal >= 0 && ordinal < VALUES.length) ? VALUES[ordinal] : UNSPECIFIED;
        }
    }

    /**
     * The entity category.
     */
    private Category category;

    public EntityDto() {
        // No-arg constructor for Fory - fields set directly
    }

    public EntityDto(String ephemeralId, EntityType entityType, String entityValue, Category category) {
        this.ephemeralId = ephemeralId;
        this.entityType = entityType;
        this.entityValue = entityValue;
        this.category = category;
    }

    public String getEphemeralId() {
        return ephemeralId;
    }

    public void setEphemeralId(String ephemeralId) {
        this.ephemeralId = ephemeralId;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    public String getEntityValue() {
        return entityValue;
    }

    public void setEntityValue(String entityValue) {
        this.entityValue = entityValue;
    }

    public String getClaims() {
        return claims;
    }

    public void setClaims(String claims) {
        this.claims = claims;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntityDto entityDto = (EntityDto) o;
        return Objects.equals(ephemeralId, entityDto.ephemeralId) &&
               entityType == entityDto.entityType &&
               Objects.equals(entityValue, entityDto.entityValue) &&
               Objects.equals(claims, entityDto.claims) &&
               category == entityDto.category;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ephemeralId, entityType, entityValue, claims, category);
    }

    @Override
    public String toString() {
        return "EntityDto{ephemeralId='" + ephemeralId + "', entityType=" + entityType +
               ", entityValue='" + entityValue + "', category=" + category + "}";
    }
}
