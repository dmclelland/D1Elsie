package com.dmc.d1.cqrs;

import com.dmc.d1.cqrs.event.AggregateEvent;
import com.dmc.d1.domain.Id;

import java.util.*;

/**
 * Created by davidclelland on 17/05/2016.
 */
public class InMemoryAggregateEventStore implements AggregateEventStore {

    List<AggregateEvent> events = new ArrayList<>();

    Map<Id, List<AggregateEvent>> eventsById = new HashMap<>();

    @Override
    public void add(AggregateEvent event) {
        events.add(event);

        List<AggregateEvent> list = eventsById.get(event.getId());
        if(list==null) {
            list = new ArrayList<>();
            eventsById.put(event.getId(), list);
        }
        list.add(event);

    }

    @Override
    public void add(Iterable<AggregateEvent> eventsToAdd) {
        for(AggregateEvent e : eventsToAdd) {
            add(e);
        }
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
    public List<AggregateEvent> get(Id id) {
        return eventsById.get(id);
    }
}
