package com.ticketing.platform.shared.exception;

/**
 * Thrown when an aggregate/entity requested by identifier cannot be found.
 */
public class EntityNotFoundException extends DomainException {

    public EntityNotFoundException(String entityName, Object id) {
        super("%s with identifier '%s' not found".formatted(entityName, id));
    }
}
