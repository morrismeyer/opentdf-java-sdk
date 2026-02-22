package io.opentdf.fory.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A logical bucket of attributes belonging to a "Resource".
 */
public class ResourceDto {
    /**
     * Unique identifier for this resource's attributes.
     */
    private String resourceAttributesId;

    /**
     * List of attribute value FQNs associated with this resource.
     */
    private List<String> attributeValueFqns;

    public ResourceDto() {
        this.attributeValueFqns = new ArrayList<>();
    }

    public ResourceDto(String resourceAttributesId, List<String> attributeValueFqns) {
        this.resourceAttributesId = resourceAttributesId;
        this.attributeValueFqns = attributeValueFqns != null ? attributeValueFqns : new ArrayList<>();
    }

    public String getResourceAttributesId() {
        return resourceAttributesId;
    }

    public void setResourceAttributesId(String resourceAttributesId) {
        this.resourceAttributesId = resourceAttributesId;
    }

    public List<String> getAttributeValueFqns() {
        return attributeValueFqns;
    }

    public void setAttributeValueFqns(List<String> attributeValueFqns) {
        this.attributeValueFqns = attributeValueFqns;
    }

    public void addAttributeValueFqn(String fqn) {
        if (this.attributeValueFqns == null) {
            this.attributeValueFqns = new ArrayList<>();
        }
        this.attributeValueFqns.add(fqn);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourceDto that = (ResourceDto) o;
        return Objects.equals(resourceAttributesId, that.resourceAttributesId) &&
               Objects.equals(attributeValueFqns, that.attributeValueFqns);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resourceAttributesId, attributeValueFqns);
    }

    @Override
    public String toString() {
        return "ResourceDto{resourceAttributesId='" + resourceAttributesId +
               "', attributeValueFqns=" + attributeValueFqns + "}";
    }
}
