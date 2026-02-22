package io.opentdf.fory.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Attribute value within an attribute definition.
 */
public class ValueDto {
    /**
     * Generated uuid in database.
     */
    private String id;

    /**
     * Parent attribute.
     */
    private AttributeDto attribute;

    /**
     * The value string.
     */
    private String value;

    /**
     * Fully Qualified Name.
     */
    private String fqn;

    /**
     * Active by default until explicitly deactivated.
     */
    private Boolean active;

    /**
     * Keys associated with the value.
     */
    private List<SimpleKasKeyDto> kasKeys;

    public ValueDto() {
        this.kasKeys = new ArrayList<>();
    }

    public ValueDto(String id, String value, String fqn) {
        this.id = id;
        this.value = value;
        this.fqn = fqn;
        this.kasKeys = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public AttributeDto getAttribute() {
        return attribute;
    }

    public void setAttribute(AttributeDto attribute) {
        this.attribute = attribute;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
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
        ValueDto valueDto = (ValueDto) o;
        return Objects.equals(id, valueDto.id) &&
               Objects.equals(value, valueDto.value) &&
               Objects.equals(fqn, valueDto.fqn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, value, fqn);
    }

    @Override
    public String toString() {
        return "ValueDto{id='" + id + "', value='" + value + "', fqn='" + fqn + "'}";
    }
}
