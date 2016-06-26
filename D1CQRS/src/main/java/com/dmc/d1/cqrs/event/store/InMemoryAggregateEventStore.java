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
    public Iterator<List<AggregateEventAbstract>> iterator() {
        return new InMemoryIterator(events);
    }


    private static class InMemoryIterator implements Iterator<List<AggregateEventAbstract>> {

        private final List<AggregateEventAbstract> lst;
        private boolean hasNext = true;

        InMemoryIterator(List<AggregateEventAbstract> lst) {
            this.lst = lst;

        }



        @Override
        public boolean hasNext() {
            return hasNext;
        }

        @Override
        public List<AggregateEventAbstract> next() {
            this.hasNext = false;

            return lst;
        }
    }


}
