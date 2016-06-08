package com.dmc.d1.cqrs;


import com.dmc.d1.cqrs.event.AggregateEvent;
import com.dmc.d1.cqrs.event.EventFactoryMarker;
import com.dmc.d1.cqrs.event.store.AggregateEventStore;
import com.dmc.d1.cqrs.event.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by davidclelland on 16/05/2016.
 */
public abstract class Aggregate<EF extends EventFactoryMarker>{

    private static final Logger LOG = LoggerFactory.getLogger(Aggregate.class);
    private final List<AggregateEvent> uncommittedEvents = new ArrayList<>();
    private AnnotatedAggregateEventHandlerInvoker eventHandler;
    private EventBus eventBus;
    private AggregateEventStore aggregateEventStore;
    protected EF eventFactory;



    protected void apply(AggregateEvent event) {
        applyAggregateEvent(event);
        addToUncommitted(event);
        eventBus.publish(event);
    }

    void replay(AggregateEvent event) {
        applyAggregateEvent(event);
    }

    protected abstract void rollbackAggregateToInitialState();

    protected abstract String getId();


    private void applyAggregateEvent(AggregateEvent event) {
        eventHandler.invoke(event, this);
    }

    void commit() {
        aggregateEventStore.add(uncommittedEvents);
        clearUncommittedEvents();
    }

    void rollback() {
        Iterable<AggregateEvent> eventsToReplay = aggregateEventStore.get(getId());
        clearUncommittedEvents();
        rollbackAggregateToInitialState();

        for (AggregateEvent event : eventsToReplay) {
            applyAggregateEvent(event);
        }
    }

    private void addToUncommitted(AggregateEvent e) {
        uncommittedEvents.add(e);
    }

    private void clearUncommittedEvents() {
        for(AggregateEvent event : uncommittedEvents){
            event.clean();
        }
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

    void setEventFactory(EF eventFactory){
        this.eventFactory = checkNotNull(eventFactory);
    }


}
