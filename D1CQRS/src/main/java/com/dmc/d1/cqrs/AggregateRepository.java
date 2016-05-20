package com.dmc.d1.cqrs;

import com.dmc.d1.cqrs.event.AggregateEvent;
import com.dmc.d1.domain.Id;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by davidclelland on 16/05/2016.
 */
public class AggregateRepository<ID extends Id, A extends Aggregate> {

    private Map<Id, A> cache  = new HashMap<>();
    private final AggregateEventStore aggregateEventStore;

    public AggregateRepository(AggregateEventStore aggregateEventStore){
        this.aggregateEventStore = checkNotNull(aggregateEventStore);
    }

    public A create(A aggregate){

        cache.put(aggregate.getId(), aggregate);
        return aggregate;
    }

    public void commit(ID id){
        A aggregate = cache.get(id);
        aggregateEventStore.add(aggregate.getUncommittedEvents());
        aggregate.clearUncommittedEvents();
    }

    public A find(ID id){
        return cache.get(id);
    }

    public void rollback(ID id){
        A aggregate = cache.get(id);

        Iterable<AggregateEvent> events = aggregateEventStore.get(id);
        aggregate.rollback(events);
    }

}