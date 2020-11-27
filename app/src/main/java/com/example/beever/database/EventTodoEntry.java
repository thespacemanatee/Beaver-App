package com.example.beever.database;

import java.util.Map;

/**
 * An abstract class on which both EventEntry and TodoEntry is based.
 * This is made mostly for convenience in modifyEventOrTodo() in UserEntry.class and GroupEntry.class.
 */
public abstract class EventTodoEntry {

    /**
     * Get equivalent Map object representation which obeys EventEntry/TodoEntry contract
     * @return Map object representation
     */
    public abstract Map<String, Object> getRepresentation();

    /**
     * Check whether this is a group (and not personal) event/todo
     * @return boolean for whether this is a group entity
     */
    public abstract boolean isGroupEntry();

    /**
     * Get user/group ID of entity containing this event/todo
     * @return user/group ID
     */
    public abstract String getSource();
}
