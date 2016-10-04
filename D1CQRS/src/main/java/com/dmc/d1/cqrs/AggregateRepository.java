package com.dmc.d1.cqrs;

import com.dmc.d1.cqrs.event.EventBus;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by davidclelland on 16/05/2016.
 */
public class AggregateRepository<A extends Aggregate> {

    private final AggregateEventStore aggregateEventStore;

    //allow for just
    // over 1_000_000 aggregates
    static int CACHE_CAPACITY = 2 << 19;

    private final Map<Long, A> cache = new HashMap<>(CACHE_CAPACITY);
    private final AnnotatedAggregateEventHandlerInvoker annotatedAggregateEventHandlerInvoker;
    private final EventBus eventBus;
    private final Class<A> aggregateClass;
    private final String aggregateClassName;

    private final Supplier<A> aggregateInstanceFactory;


    public AggregateRepository(AggregateEventStore aggregateEventStore,
                               Class<A> aggregateClass,
                               EventBus eventBus,
                               Supplier<A> aggregateInstanceFactory
    ) {
        this.aggregateEventStore = checkNotNull(aggregateEventStore);
        this.aggregateClass = checkNotNull(aggregateClass);
        this.aggregateClassName = aggregateClass.getName();
        this.annotatedAggregateEventHandlerInvoker = checkNotNull(getEventHandler());
        this.eventBus = checkNotNull(eventBus);
        this.aggregateInstanceFactory = checkNotNull(aggregateInstanceFactory);
    }


    final A create(long id) {
        //initialized event used for replays - the event is a marker to indicate
        //that a new aggregate of the specified type needs to be created
        AggregateInitialisedEvent initialisationEvent = new AggregateInitialisedEvent();
        initialisationEvent.setAggregateId(id);
        initialisationEvent.setAggregateClassName(aggregateClassName);

        A aggregate = handleAggregateInitialisedEvent(initialisationEvent);

        aggregate.apply(initialisationEvent);

        return aggregate;
    }

    final void rollback(A old) {
        //when rolling back old we need to set a copy of it
        AggregateInitialisedEvent initialisationEvent = new AggregateInitialisedEvent();
        initialisationEvent.setAggregateId(old.getId());
        initialisationEvent.setAggregateClassName(aggregateClassName);

        A newOld = aggregateInstanceFactory.get();
        initialise(newOld, initialisationEvent);

        //now copy the state
        newOld.stateCopy(old);

        old.setOld(newOld);

        //update cache with old
        cache.put(old.getId(), old);
    }


    final A handleAggregateInitialisedEvent(AggregateInitialisedEvent event) {
        A aggregate = aggregateInstanceFactory.get();
        initialise(aggregate, event);

        //copy used for rollback
        A old = aggregateInstanceFactory.get();
        initialise(old, event);
        aggregate.setOld(old);

        cache.put(aggregate.getId(), aggregate);
        return aggregate;
    }

    private void initialise(A aggregate, AggregateInitialisedEvent event) {
        aggregate.setId(event.getAggregateId());
        aggregate.setAggregateClassName(event.getAggregateClassName());

        aggregate.setEventHandler(annotatedAggregateEventHandlerInvoker);
        aggregate.setEventBus(eventBus);
        aggregate.setAggregateEventStore(aggregateEventStore);
        aggregate.setRepository(this);
    }

    public final A find(long id) {
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

    public final String getAggregateClassName() {
        return aggregateClassName;
    }
}