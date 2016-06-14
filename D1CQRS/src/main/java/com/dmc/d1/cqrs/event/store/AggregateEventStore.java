package com.dmc.d1.cqrs.event.store;

import com.dmc.d1.cqrs.event.AggregateEvent;
import com.dmc.d1.cqrs.event.AggregateEventAbstract;

import java.util.List;

/**
 * Created by davidclelland on 16/05/2016.
 */
public interface AggregateEventStore<E extends AggregateEvent> {

    void add(E event);

    void add(List<E> events);

    List<E> get(String id);

    List<E> getAll();
}