package io.opentdf.fory.dto;

import java.util.Objects;

/**
 * An action an entity can take.
 */
public class ActionDto {
    /**
     * Standard action enumeration.
     */
    public enum StandardAction {
        UNSPECIFIED,
        DECRYPT,
        TRANSMIT
    }

    /**
     * Generated uuid in database.
     */
    private String id;

    /**
     * Standard action type.
     */
    private StandardAction standardAction;

    /**
     * Custom action name.
     */
    private String customAction;

    /**
     * Action name.
     */
    private String name;

    public ActionDto() {
    }

    public ActionDto(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public static ActionDto standard(StandardAction action) {
        ActionDto dto = new ActionDto();
        dto.setStandardAction(action);
        return dto;
    }

    public static ActionDto custom(String action) {
        ActionDto dto = new ActionDto();
        dto.setCustomAction(action);
        return dto;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public StandardAction getStandardAction() {
        return standardAction;
    }

    public void setStandardAction(StandardAction standardAction) {
        this.standardAction = standardAction;
    }

    public String getCustomAction() {
        return customAction;
    }

    public void setCustomAction(String customAction) {
        this.customAction = customAction;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActionDto actionDto = (ActionDto) o;
        return Objects.equals(id, actionDto.id) &&
               standardAction == actionDto.standardAction &&
               Objects.equals(customAction, actionDto.customAction) &&
               Objects.equals(name, actionDto.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, standardAction, customAction, name);
    }

    @Override
    public String toString() {
        if (name != null) {
            return "ActionDto{name='" + name + "'}";
        } else if (standardAction != null) {
            return "ActionDto{standardAction=" + standardAction + "}";
        } else if (customAction != null) {
            return "ActionDto{customAction='" + customAction + "'}";
        }
        return "ActionDto{id='" + id + "'}";
    }
}
