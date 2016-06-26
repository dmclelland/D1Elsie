package com.dmc.d1.cqrs;

import com.dmc.d1.cqrs.event.AggregateEvent;
import com.dmc.d1.cqrs.event.AggregateInitialisedEvent;
import com.dmc.d1.cqrs.event.store.AggregateEventStore;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created By davidclelland on 12/06/2016.
 */
public class AggregateEventReplayer {

    private final AggregateEventStore eventStore;
    private final Map<String, AggregateRepository> repos;

    public AggregateEventReplayer(AggregateEventStore eventStore,
                                  List<AggregateRepository> repos) {

        this.eventStore = checkNotNull(eventStore);
        checkNotNull(repos);
        this.repos = repos.stream().collect(Collectors.toMap(a -> a.getAggregateClassName(), a -> a));
    }


    public void replay() {

        Iterator<List<AggregateEvent>> batches = eventStore.iterator();
        AggregateRepository repo;
        Aggregate agg;
        List<AggregateEvent> events;
        while(batches.hasNext()){
            events =  batches.next();

            for (AggregateEvent event : events) {
                repo = repos.get(event.getAggregateClassName());
                if (event instanceof AggregateInitialisedEvent) {
                    repo.handleAggregateInitialisedEvent((AggregateInitialisedEvent) event);
                } else {
                    agg = repo.find(event.getAggregateId());
                    agg.replay(event);
                }
            }
        }
    }
}
