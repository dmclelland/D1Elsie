package com.dmc.d1.cqrs;

import com.dmc.d1.cqrs.event.AggregateEvent;

import java.util.List;

/**
 * Created by davidclelland on 16/05/2016.
 */
public interface AggregateEventStore {

    void add(AggregateEvent event);

    void add(List<AggregateEvent> events);

    AggregateEvent get();

    List<AggregateEvent> getAll();

    List<AggregateEvent> get(String id);
}
