package com.dmc.d1.cqrs;


import com.dmc.d1.cqrs.event.AggregateEvent;
import com.dmc.d1.domain.Id;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by davidclelland on 16/05/2016.
 */
public abstract class Aggregate<ID extends Id> {

    private static final Logger LOG = LoggerFactory.getLogger(Aggregate.class);

    private final static AggregateEventHandler EVENT_HANDLER = new ReflectiveAggregateEventHandler();

    private final List<AggregateEvent> uncommittedEvents = new ArrayList<>();

    public void apply(AggregateEvent event) {
        applyAggregateEvent(event);
        //TODO apply to external event handlers
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
            EVENT_HANDLER.invoke(event, this);
        } catch (Exception e) {
            LOG.error("Unable to apply event {} ", event.toString(), e);
        }
    }


    void addToUncommitted(AggregateEvent e) {
        uncommittedEvents.add(e);
    }

    List<AggregateEvent> getUncommittedEvents() {
        return uncommittedEvents;
    }

    void clearUncommittedEvents() {
        uncommittedEvents.clear();
    }

    protected abstract ID getId();




}
