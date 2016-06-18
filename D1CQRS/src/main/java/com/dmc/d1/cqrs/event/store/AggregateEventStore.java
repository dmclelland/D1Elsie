package com.dmc.d1.cqrs.event.store;

import com.dmc.d1.cqrs.event.AggregateEvent;
import com.dmc.d1.cqrs.event.AggregateEventAbstract;

import java.util.Iterator;
import java.util.List;

/**
 * Created by davidclelland on 16/05/2016.
 */
public interface AggregateEventStore<E extends AggregateEvent> extends Iterable<List<E>>{

    void add(E event);

    void add(List<E> events);
}
