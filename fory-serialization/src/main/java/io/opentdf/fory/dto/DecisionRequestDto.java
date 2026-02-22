package io.opentdf.fory.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Request for authorization decisions.
 */
public class DecisionRequestDto {
    /**
     * List of actions to evaluate.
     */
    private List<ActionDto> actions;

    /**
     * List of entity chains to evaluate.
     */
    private List<EntityChainDto> entityChains;

    /**
     * List of resource attributes to evaluate against.
     */
    private List<ResourceDto> resourceAttributes;

    public DecisionRequestDto() {
        this.actions = new ArrayList<>();
        this.entityChains = new ArrayList<>();
        this.resourceAttributes = new ArrayList<>();
    }

    public List<ActionDto> getActions() {
        return actions;
    }

    public void setActions(List<ActionDto> actions) {
        this.actions = actions;
    }

    public void addAction(ActionDto action) {
        if (this.actions == null) {
            this.actions = new ArrayList<>();
        }
        this.actions.add(action);
    }

    public List<EntityChainDto> getEntityChains() {
        return entityChains;
    }

    public void setEntityChains(List<EntityChainDto> entityChains) {
        this.entityChains = entityChains;
    }

    public void addEntityChain(EntityChainDto entityChain) {
        if (this.entityChains == null) {
            this.entityChains = new ArrayList<>();
        }
        this.entityChains.add(entityChain);
    }

    public List<ResourceDto> getResourceAttributes() {
        return resourceAttributes;
    }

    public void setResourceAttributes(List<ResourceDto> resourceAttributes) {
        this.resourceAttributes = resourceAttributes;
    }

    public void addResourceAttribute(ResourceDto resource) {
        if (this.resourceAttributes == null) {
            this.resourceAttributes = new ArrayList<>();
        }
        this.resourceAttributes.add(resource);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DecisionRequestDto that = (DecisionRequestDto) o;
        return Objects.equals(actions, that.actions) &&
               Objects.equals(entityChains, that.entityChains) &&
               Objects.equals(resourceAttributes, that.resourceAttributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(actions, entityChains, resourceAttributes);
    }

    @Override
    public String toString() {
        return "DecisionRequestDto{actions=" + actions +
               ", entityChains=" + entityChains +
               ", resourceAttributes=" + resourceAttributes + "}";
    }
}
