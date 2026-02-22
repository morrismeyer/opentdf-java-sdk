package io.opentdf.fory.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Attribute definition within a namespace.
 */
public class AttributeDto {
    /**
     * Attribute rule type enumeration.
     */
    public enum RuleType {
        UNSPECIFIED,
        ALL_OF,
        ANY_OF,
        HIERARCHY
    }

    /**
     * Generated uuid in database.
     */
    private String id;

    /**
     * Namespace of the attribute.
     */
    private NamespaceDto namespace;

    /**
     * Attribute name.
     */
    private String name;

    /**
     * Attribute rule type.
     */
    private RuleType rule;

    /**
     * Attribute values.
     */
    private List<ValueDto> values;

    /**
     * Fully Qualified Name.
     */
    private String fqn;

    /**
     * Active by default until explicitly deactivated.
     */
    private Boolean active;

    /**
     * Keys associated with the attribute.
     */
    private List<SimpleKasKeyDto> kasKeys;

    /**
     * Whether to use the attribute definition during encryption if the attribute value is missing.
     */
    private Boolean allowTraversal;

    public AttributeDto() {
        this.values = new ArrayList<>();
        this.kasKeys = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public NamespaceDto getNamespace() {
        return namespace;
    }

    public void setNamespace(NamespaceDto namespace) {
        this.namespace = namespace;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RuleType getRule() {
        return rule;
    }

    public void setRule(RuleType rule) {
        this.rule = rule;
    }

    public List<ValueDto> getValues() {
        return values;
    }

    public void setValues(List<ValueDto> values) {
        this.values = values;
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

    public Boolean getAllowTraversal() {
        return allowTraversal;
    }

    public void setAllowTraversal(Boolean allowTraversal) {
        this.allowTraversal = allowTraversal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AttributeDto that = (AttributeDto) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(name, that.name) &&
               Objects.equals(fqn, that.fqn) &&
               rule == that.rule;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, fqn, rule);
    }

    @Override
    public String toString() {
        return "AttributeDto{id='" + id + "', name='" + name + "', fqn='" + fqn + "', rule=" + rule + "}";
    }
}
