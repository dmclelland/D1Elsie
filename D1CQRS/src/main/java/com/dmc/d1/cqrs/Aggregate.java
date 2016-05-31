package com.dmc.d1.cqrs;


import com.dmc.d1.cqrs.event.AggregateEvent;
import com.dmc.d1.cqrs.event.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by davidclelland on 16/05/2016.
 */
public abstract class Aggregate {

    private static final Logger LOG = LoggerFactory.getLogger(Aggregate.class);
    private final List<AggregateEvent> uncommittedEvents = new ArrayList<>();
    private AnnotatedAggregateEventHandlerInvoker eventHandler;
    private EventBus eventBus;


    public void apply(AggregateEvent event) {
        applyAggregateEvent(event);
        addToUncommitted(event);
        eventBus.publish(event);
    }

    public void replay(AggregateEvent event) {
        applyAggregateEvent(event);
    }

    //to rollback reset and replay the events
    void rollback( Iterable<AggregateEvent> events) {
        clearUncommittedEvents();
        rollbackAggregateToInitialState();

        for(AggregateEvent event : events) {
            applyAggregateEvent(event);
        }
    }

    protected abstract void rollbackAggregateToInitialState();

    private void applyAggregateEvent(AggregateEvent event) {
        try {
            eventHandler.invoke(event, this);
        } catch (Exception e) {
            LOG.error("Unable to apply event {} ", event.toString(), e);
        }
    }

    private void addToUncommitted(AggregateEvent e) {
        uncommittedEvents.add(e);
    }

    List<AggregateEvent> getUncommittedEvents() {
        return uncommittedEvents;
    }

    void clearUncommittedEvents() {
        uncommittedEvents.clear();
    }

    protected abstract String getId();

    void setEventHandler(AnnotatedAggregateEventHandlerInvoker eventHandler) {
        this.eventHandler = eventHandler;
    }

    void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }
}
