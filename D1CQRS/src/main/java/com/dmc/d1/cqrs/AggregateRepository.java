package com.dmc.d1.cqrs;

import com.dmc.d1.cqrs.event.AggregateEvent;
import com.dmc.d1.cqrs.event.AggregateInitialisedEvent;
import com.dmc.d1.cqrs.event.EventBus;
import com.dmc.d1.cqrs.event.EventFactory;
import com.dmc.d1.cqrs.event.store.AggregateEventStore;

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
    private final AggregateFactory<A> aggregateFactory;

    private final Class<A> aggregateClass;
    private final String aggregateClassName;

    private final EventFactory eventFactory;

    public AggregateRepository(AggregateEventStore aggregateEventStore, Class<A> aggregateClass, EventBus eventBus,
                               EventFactory eventFactory, AggregateFactory<A> aggregateFactory) {
        this.aggregateEventStore = checkNotNull(aggregateEventStore);
        this.aggregateClass = checkNotNull(aggregateClass);
        this.aggregateClassName = aggregateClass.getName();
        this.annotatedAggregateEventHandlerInvoker = checkNotNull(getEventHandler());
        this.eventBus = checkNotNull(eventBus);
        this.eventFactory = checkNotNull(eventFactory);
        this.aggregateFactory = checkNotNull(aggregateFactory);
    }

    A create(String id) {

       //initialized event used for replays - the event is a marker to indicate
        //that a new aggregate of the specified type needs to be created
        AggregateEvent event = eventFactory.createAggregateInitialisedEvent(id);

        A aggregate = handleAggregateInitialisedEvent(event);
        aggregate.apply(event);

        return aggregate;
    }

    A handleAggregateInitialisedEvent(AggregateEvent event) {
        A aggregate = aggregateFactory.create(event.getAggregateId(), aggregateClassName);

        aggregate.setEventHandler(annotatedAggregateEventHandlerInvoker);
        aggregate.setEventBus(eventBus);
        aggregate.setAggregateEventStore(aggregateEventStore);
        aggregate.setEventFactory(eventFactory);

        cache.put(aggregate.getId(), aggregate);
        return aggregate;
    }

    A find(String id) {
        return cache.get(id);
    }

    private AnnotatedAggregateEventHandlerInvoker getEventHandler() {
        try {
            String className = aggregateClass.getSimpleName() + "AnnotatedMethodInvoker";
            Class<?> clazz = Class.forName("com.dmc.d1.algo.eventhandler." + className);
            return (AnnotatedAggregateEventHandlerInvoker) clazz.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Unable to resolve annotated method invoker for " + aggregateClassName, e);
        }
    }

    String getAggregateClassName() {
        return aggregateClassName;
    }
}