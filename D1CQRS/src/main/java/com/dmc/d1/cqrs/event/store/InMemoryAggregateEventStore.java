package com.dmc.d1.cqrs.event.store;

import com.dmc.d1.cqrs.event.AggregateEvent;

import java.util.*;

/**
 * Created by davidclelland on 17/05/2016.
 */
public class InMemoryAggregateEventStore implements AggregateEventStore<AggregateEvent> {

    List<AggregateEvent> events = new ArrayList<>();

    Map<String, List<AggregateEvent>> eventsById = new HashMap<>();

    @Override
    public void add(AggregateEvent event) {
        events.add(event);

        List<AggregateEvent> list = eventsById.get(event.getAggregateId());
        if(list==null) {
            list = new ArrayList<>();
            eventsById.put(event.getAggregateId(), list);
        }
        list.add(event);

    }

    @Override
    public void add(List<AggregateEvent> eventsToAdd) {
        eventsToAdd.forEach(this::add);
    }

    @Override
    public AggregateEvent get() {
        return events.get(events.size()-1);
    }

    @Override
    public List<AggregateEvent> getAll() {
        return events;
    }

    @Override
    public List<AggregateEvent> get(String id) {
        return eventsById.get(id);
    }
}
