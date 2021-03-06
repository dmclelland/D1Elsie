package com.dmc.d1.cqrs;

import com.dmc.d1.cqrs.event.AggregateEvent;

import java.util.List;
import java.util.Map;

/**
 * Created by davidclelland on 16/05/2016.
 */
public interface AggregateEventStore<E extends AggregateEvent> {

    void add(E event);

    void add(List<E> events);

    void replay(List<AggregateRepository> repos);
}
