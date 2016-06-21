package com.dmc.d1.cqrs;

import com.dmc.d1.cqrs.event.AggregateInitialisedEvent;
import com.dmc.d1.cqrs.event.EventBus;
import com.dmc.d1.cqrs.event.store.AggregateEventStore;
import com.dmc.d1.cqrs.util.NewInstanceFactory;

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
    private final Class<A> aggregateClass;
    private final String aggregateClassName;

    private final NewInstanceFactory<A> newInstanceFactory;
    private final InitialisationEventFactory<AggregateInitialisedEvent> initialisationEventFactory;

    public AggregateRepository(AggregateEventStore aggregateEventStore,
                               Class<A> aggregateClass,
                               EventBus eventBus,
                               NewInstanceFactory<A> aggregateInstanceFactory,
                               InitialisationEventFactory<AggregateInitialisedEvent> initialisationEventFactory
                               ) {
        this.aggregateEventStore = checkNotNull(aggregateEventStore);
        this.aggregateClass = checkNotNull(aggregateClass);
        this.aggregateClassName = aggregateClass.getName();
        this.annotatedAggregateEventHandlerInvoker = checkNotNull(getEventHandler());
        this.eventBus = checkNotNull(eventBus);
        this.newInstanceFactory = checkNotNull(aggregateInstanceFactory);
        this.initialisationEventFactory = checkNotNull(initialisationEventFactory);
    }

    final A create(String id) {
        //initialized event used for replays - the event is a marker to indicate
        //that a new aggregate of the specified type needs to be created
        AggregateInitialisedEvent initialisationEvent = initialisationEventFactory.newInstance(id);
        initialisationEvent.setAggregateClassName(aggregateClassName);
        A aggregate = handleAggregateInitialisedEvent(initialisationEvent);
        aggregate.apply(initialisationEvent);
        return aggregate;
    }

    final A handleAggregateInitialisedEvent(AggregateInitialisedEvent event) {
        A aggregate = newInstanceFactory.newInstance();
        aggregate.setId(event.getAggregateId());
        aggregate.setAggregateClassName(event.getAggregateClassName());

        aggregate.setEventHandler(annotatedAggregateEventHandlerInvoker);
        aggregate.setEventBus(eventBus);
        aggregate.setAggregateEventStore(aggregateEventStore);

        //copy used for rollback
        A old = newInstanceFactory.newInstance();
        old.copy(aggregate);
        aggregate.setOld(old);

        cache.put(aggregate.getId(), aggregate);
        return aggregate;
    }

    final A find(String id) {
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

    final String getAggregateClassName() {
        return aggregateClassName;
    }
}