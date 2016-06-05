package com.dmc.d1.cqrs.event.store;

import com.dmc.d1.cqrs.event.AggregateEvent;

import java.util.List;

/**
 * Created by davidclelland on 16/05/2016.
 */
public interface AggregateEventStore<E extends AggregateEvent> {

    void add(E event);

    void add(List<E> events);

    E get();

    List<E> getAll();

    List<E> get(String id);
}
