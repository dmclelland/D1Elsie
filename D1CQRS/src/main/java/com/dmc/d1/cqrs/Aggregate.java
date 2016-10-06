package com.dmc.d1.cqrs;


import com.dmc.d1.cqrs.event.AggregateEvent;
import com.dmc.d1.cqrs.event.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by davidclelland on 16/05/2016.
 */
public abstract class Aggregate<A extends Aggregate<A>> {

    private static final Logger LOG = LoggerFactory.getLogger(Aggregate.class);

    private final List<AggregateEvent> uncommittedEvents = new ArrayList<>();
    private AnnotatedAggregateEventHandlerInvoker eventHandler;
    private EventBus eventBus;
    private AggregateEventStore<AggregateEvent> aggregateEventStore;
    private AggregateRepository<A> repository;
    private long id;

    private A old;

    protected final <E extends AggregateEvent> void apply(E event) {

        applyAggregateEvent(event);
        addToUncommitted(event);
        eventBus.publish(event);
    }

    public final void replay(AggregateEvent event) {
        eventHandler.invoke(event, this);
        eventHandler.invoke(event, this.old);
    }

    protected final long getId() {
        return id;
    }

    final void applyAggregateEvent(AggregateEvent event) {
        eventHandler.invoke(event, this);
    }

    final void commit() {
        aggregateEventStore.add(uncommittedEvents);
        //apply events to old
        for (AggregateEvent event : uncommittedEvents) {
            old.applyAggregateEvent(event);
        }
        clearUncommittedEvents();
    }

    protected abstract A stateCopy(A o);

    final void rollback() {
        clearUncommittedEvents();
        repository.rollback(this.old);
    }

    final void setEventHandler(AnnotatedAggregateEventHandlerInvoker eventHandler) {
        this.eventHandler = checkNotNull(eventHandler);
    }

    final void setEventBus(EventBus eventBus) {
        this.eventBus = checkNotNull(eventBus);
    }

    final void setAggregateEventStore(AggregateEventStore aggregateEventStore) {
        this.aggregateEventStore = checkNotNull(aggregateEventStore);
    }

    final void setId(long id) {
        this.id = id;
    }

    final void setRepository(AggregateRepository<A> repository) {
        this.repository = repository;
    }

    final void setOld(Aggregate old) {
        this.old = (A) old;
    }

    private void addToUncommitted(AggregateEvent e) {
        uncommittedEvents.add(e);
    }

    private void clearUncommittedEvents() {
        uncommittedEvents.clear();
    }

}
