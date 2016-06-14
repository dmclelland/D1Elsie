package com.dmc.d1.cqrs.event.store;

import com.dmc.d1.cqrs.event.AggregateEventAbstract;

import java.util.*;

/**
 * Created by davidclelland on 17/05/2016.
 */
public class InMemoryAggregateEventStore implements AggregateEventStore<AggregateEventAbstract> {

    List<AggregateEventAbstract> events = new ArrayList<>();

    Map<String, List<AggregateEventAbstract>> eventsById = new HashMap<>();

    @Override
    public void add(AggregateEventAbstract event) {
        events.add(event);

        List<AggregateEventAbstract> list = eventsById.get(event.getAggregateId());
        if(list==null) {
            list = new ArrayList<>();
            eventsById.put(event.getAggregateId(), list);
        }
        list.add(event);
    }

    @Override
    public void add(List<AggregateEventAbstract> eventsToAdd) {
        eventsToAdd.forEach(this::add);
    }

    @Override
    public List<AggregateEventAbstract> getAll() {
        return events;
    }

    @Override
    public List<AggregateEventAbstract> get(String id) {
        return eventsById.get(id);
    }
}
