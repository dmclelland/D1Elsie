package com.dmc.d1.cqrs;

import com.dmc.d1.cqrs.event.store.AggregateEventStore;
import com.dmc.d1.cqrs.event.EventBus;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by davidclelland on 16/05/2016.
 */
public class AggregateRepository<A extends Aggregate> {

    private final AggregateEventStore aggregateEventStore;
    private final Map<String, A> cache = new HashMap<>();
    private final AnnotatedAggregateEventHandlerInvoker annotatedAggregateEventHandlerInvoker;
    private final EventBus eventBus;

    private final Class<A> aggregateType;

    public AggregateRepository(AggregateEventStore aggregateEventStore, Class<A> aggregateType, EventBus eventBus) {
        this.aggregateEventStore = checkNotNull(aggregateEventStore);
        this.aggregateType = checkNotNull(aggregateType);
        this.annotatedAggregateEventHandlerInvoker = checkNotNull(getEventHandler());
        this.eventBus = checkNotNull(eventBus);
    }

     A create(A aggregate) {
        //create aggregate event handler
        aggregate.setEventHandler(annotatedAggregateEventHandlerInvoker);
        aggregate.setEventBus(eventBus);
        aggregate.setAggregateEventStore(aggregateEventStore);
        cache.put(aggregate.getId(), aggregate);

        return aggregate;
    }


     A find(String id) {
        return cache.get(id);
    }


    private AnnotatedAggregateEventHandlerInvoker getEventHandler() {
        try {
            String className = aggregateType.getSimpleName() + "AnnotatedMethodInvoker";
            Class<?> clazz = Class.forName("com.dmc.d1.algo.eventhandler." + className);
            return (AnnotatedAggregateEventHandlerInvoker) clazz.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Unable to resolve annotated method invoker for " +aggregateType.getName(), e);
        }
    }

}