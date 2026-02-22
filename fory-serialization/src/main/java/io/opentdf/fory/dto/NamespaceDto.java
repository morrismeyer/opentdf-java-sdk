package io.opentdf.fory.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Namespace for partitioning Attribute Definitions.
 */
public class NamespaceDto {
    /**
     * Generated uuid in database.
     */
    private String id;

    /**
     * Used to partition Attribute Definitions and enable federation.
     */
    private String name;

    /**
     * Fully Qualified Name.
     */
    private String fqn;

    /**
     * Active by default until explicitly deactivated.
     */
    private Boolean active;

    /**
     * Keys for the namespace.
     */
    private List<SimpleKasKeyDto> kasKeys;

    public NamespaceDto() {
        this.kasKeys = new ArrayList<>();
    }

    public NamespaceDto(String id, String name, String fqn) {
        this.id = id;
        this.name = name;
        this.fqn = fqn;
        this.kasKeys = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFqn() {
        return fqn;
    }

    public void setFqn(String fqn) {
        this.fqn = fqn;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public List<SimpleKasKeyDto> getKasKeys() {
        return kasKeys;
    }

    public void setKasKeys(List<SimpleKasKeyDto> kasKeys) {
        this.kasKeys = kasKeys;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NamespaceDto that = (NamespaceDto) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(name, that.name) &&
               Objects.equals(fqn, that.fqn) &&
               Objects.equals(active, that.active);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, fqn, active);
    }

    @Override
    public String toString() {
        return "NamespaceDto{id='" + id + "', name='" + name + "', fqn='" + fqn + "'}";
    }
}
