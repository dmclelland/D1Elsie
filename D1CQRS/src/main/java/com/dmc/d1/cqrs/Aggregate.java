package com.dmc.d1.cqrs;


import com.dmc.d1.cqrs.event.AggregateEvent;
import com.dmc.d1.cqrs.event.EventBus;
import com.dmc.d1.cqrs.event.store.AggregateEventStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by davidclelland on 16/05/2016.
 */
public abstract class Aggregate {

    private static final Logger LOG = LoggerFactory.getLogger(Aggregate.class);
    private final List<AggregateEvent> uncommittedEvents = new ArrayList<>();
    private AnnotatedAggregateEventHandlerInvoker eventHandler;
    private EventBus eventBus;
    private AggregateEventStore aggregateEventStore;
    private String id;

    //concrete class name of this aggregate

    private String aggregateClassName;

    protected Aggregate() {
    }


    protected final <E extends AggregateEvent> void apply(E event) {
        //assign the aggregate class that raised the event
        //this is used if events need to be replayed
        event.setAggregateClassName(aggregateClassName);
        applyAggregateEvent(event);
        addToUncommitted(event);
        eventBus.publish(event);
    }

    void replay(AggregateEvent event) {
        applyAggregateEvent(event);
    }

    protected abstract void rollbackAggregateToInitialState();

    protected final String getId() {
        return id;
    }

    private void applyAggregateEvent(AggregateEvent event) {
        eventHandler.invoke(event, this);
    }

    final void commit() {
        aggregateEventStore.add(uncommittedEvents);
        clearUncommittedEvents();
    }

    final void rollback() {
        clearUncommittedEvents();
        rollbackAggregateToInitialState();
        Iterator<List<AggregateEvent>> batches = aggregateEventStore.iterator();
        List<AggregateEvent> events;
        while (batches.hasNext()) {
            events = batches.next();
            for (AggregateEvent event : events) {
                if (getId().equals(event.getAggregateId())) {
                    applyAggregateEvent(event);
                }
            }
        }
    }

    private void addToUncommitted(AggregateEvent e) {
        uncommittedEvents.add(e);
    }

    private void clearUncommittedEvents() {
        uncommittedEvents.clear();
    }

    void setEventHandler(AnnotatedAggregateEventHandlerInvoker eventHandler) {
        this.eventHandler = checkNotNull(eventHandler);
    }

    void setEventBus(EventBus eventBus) {
        this.eventBus = checkNotNull(eventBus);
    }

    void setAggregateEventStore(AggregateEventStore aggregateEventStore) {
        this.aggregateEventStore = checkNotNull(aggregateEventStore);
    }

    void setId(String id) {
        this.id = id;
    }

    void setAggregateClassName(String aggregateClassName) {
        this.aggregateClassName = aggregateClassName;
    }
}
