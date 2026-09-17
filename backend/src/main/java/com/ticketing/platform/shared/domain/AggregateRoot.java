package com.ticketing.platform.shared.domain;

/**
 * Marker interface for Domain Aggregate Roots in Clean Architecture.
 *
 * @param <ID> Type of aggregate identifier
 */
public interface AggregateRoot<ID> {

    ID getId();
}
