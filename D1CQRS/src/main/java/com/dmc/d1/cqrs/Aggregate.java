package com.dmc.d1.cqrs;


import com.dmc.d1.cqrs.event.AggregateEvent;
import com.dmc.d1.cqrs.event.AggregateEventAbstract;
import com.dmc.d1.cqrs.event.EventFactory;
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
public abstract class Aggregate<EF extends EventFactory>{

    private static final Logger LOG = LoggerFactory.getLogger(Aggregate.class);
    private final List<AggregateEvent> uncommittedEvents = new ArrayList<>();
    private AnnotatedAggregateEventHandlerInvoker eventHandler;
    private EventBus eventBus;
    private AggregateEventStore aggregateEventStore;
    private final String id;

    //concrete class name of this aggregate, no need for reflection as aggregate factories
    //have this information
    private final String aggregateClassName;

    protected EF eventFactory;

    protected Aggregate(String id, String aggregateClassName){
        this.id = checkNotNull(id);
        this.aggregateClassName = checkNotNull(aggregateClassName);
    }

    protected final  <E extends AggregateEvent> void apply(E event) {
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

    protected final String getId(){
        return id;
    };

    private void applyAggregateEvent(AggregateEvent event) {
        eventHandler.invoke(event, this);
    }

    final void commit() {
        aggregateEventStore.add(uncommittedEvents);
        clearUncommittedEvents();
    }

    final void rollback() {
        Iterable<AggregateEventAbstract> eventsToReplay = aggregateEventStore.get(getId());
        clearUncommittedEvents();
        rollbackAggregateToInitialState();

        for (AggregateEventAbstract event : eventsToReplay) {
            applyAggregateEvent(event);
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

    void setEventFactory(EF eventFactory){
        this.eventFactory = checkNotNull(eventFactory);
    }
}
