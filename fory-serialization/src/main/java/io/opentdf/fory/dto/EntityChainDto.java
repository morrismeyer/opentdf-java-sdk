package io.opentdf.fory.dto;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * A set of related Person Entities (PE) and Non-Person Entities (NPE).
 *
 * Optimized for Fory serialization:
 * - Uses EntityDto[] instead of List for contiguous storage (Section 12a)
 * - No defensive ArrayList allocation in no-arg constructor (Section 0)
 * - Final class enables JIT devirtualization (Section 4)
 */
public final class EntityChainDto {
    /**
     * Ephemeral id for tracking between request and response.
     */
    private String ephemeralId;

    /**
     * The array of entities in this chain.
     * Using array instead of List for better serialization performance.
     */
    private EntityDto[] entities;

    public EntityChainDto() {
        // No allocation - Fory will set fields directly
    }

    public EntityChainDto(String ephemeralId, EntityDto[] entities) {
        this.ephemeralId = ephemeralId;
        this.entities = entities;
    }

    /**
     * Convenience constructor accepting List for compatibility.
     */
    public EntityChainDto(String ephemeralId, List<EntityDto> entities) {
        this.ephemeralId = ephemeralId;
        this.entities = entities != null ? entities.toArray(new EntityDto[0]) : null;
    }

    public String getEphemeralId() {
        return ephemeralId;
    }

    public void setEphemeralId(String ephemeralId) {
        this.ephemeralId = ephemeralId;
    }

    /**
     * Returns the entities array directly (no copy for performance).
     */
    public EntityDto[] getEntities() {
        return entities;
    }

    /**
     * Returns entities as List for compatibility.
     */
    public List<EntityDto> getEntitiesAsList() {
        return entities != null ? Arrays.asList(entities) : null;
    }

    public void setEntities(EntityDto[] entities) {
        this.entities = entities;
    }

    /**
     * Sets entities from List for compatibility.
     */
    public void setEntities(List<EntityDto> entities) {
        this.entities = entities != null ? entities.toArray(new EntityDto[0]) : null;
    }

    /**
     * Returns the number of entities.
     */
    public int entityCount() {
        return entities != null ? entities.length : 0;
    }

    /**
     * Returns entity at index (direct array access, no bounds check overhead).
     */
    public EntityDto entityAt(int index) {
        return entities[index];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntityChainDto that = (EntityChainDto) o;
        return Objects.equals(ephemeralId, that.ephemeralId) && Arrays.equals(entities, that.entities);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(ephemeralId);
        result = 31 * result + Arrays.hashCode(entities);
        return result;
    }

    @Override
    public String toString() {
        return "EntityChainDto{ephemeralId='" + ephemeralId + "', entities=" + Arrays.toString(entities) + "}";
    }
}
