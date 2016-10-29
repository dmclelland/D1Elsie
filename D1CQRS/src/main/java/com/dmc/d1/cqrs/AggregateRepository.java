package com.dmc.d1.cqrs;

import com.dmc.d1.cqrs.event.EventBus;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;

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

    private final Long2ObjectMap<A> cache = new Long2ObjectLinkedOpenHashMap<>(CACHE_CAPACITY);
    private final AnnotatedAggregateEventHandlerInvoker annotatedAggregateEventHandlerInvoker;
    private final EventBus eventBus;

    private final Class<A> aggregateClass;
    private final String aggregateClassName;

    private final Supplier<A> aggregateInstanceFactory;


    public AggregateRepository(AggregateEventStore aggregateEventStore,
                               EventBus eventBus,
                               Supplier<A> aggregateInstanceFactory
    ) {
        this.aggregateEventStore = checkNotNull(aggregateEventStore);
        this.eventBus = checkNotNull(eventBus);
        this.aggregateClass = (Class<A>) aggregateInstanceFactory.get().getClass();
        this.aggregateClassName = this.aggregateClass.getName();
        this.aggregateInstanceFactory = checkNotNull(aggregateInstanceFactory);
        this.annotatedAggregateEventHandlerInvoker = checkNotNull(getEventHandler());


        cache.defaultReturnValue(null);
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

        aggregate.setEventHandler(annotatedAggregateEventHandlerInvoker);
        aggregate.setEventBus(eventBus);
        aggregate.setAggregateEventStore(aggregateEventStore);
        aggregate.setRepository(this);
    }

    public final A find(long id) {
        return cache.get(id);
    }

    public final String getAggregateClassName() {
        return aggregateClassName;
    }

    private AnnotatedAggregateEventHandlerInvoker getEventHandler() {

        try {
            String className = this.aggregateClass.getSimpleName() + "AnnotatedMethodInvoker";
            Class<?> clazz = Class.forName("com.dmc.d1.algo.eventhandler." + className);
            return (AnnotatedAggregateEventHandlerInvoker) clazz.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Unable to resolve annotated method invoker for " + this.aggregateClass.getName(), e);
        }
    }
}