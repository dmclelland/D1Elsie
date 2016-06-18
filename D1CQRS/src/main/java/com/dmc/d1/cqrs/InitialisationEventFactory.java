package com.dmc.d1.cqrs;

import com.dmc.d1.cqrs.event.AggregateInitialisedEvent;

/**
 * Created By davidclelland on 09/06/2016.
 */
public interface InitialisationEventFactory<E extends AggregateInitialisedEvent> {

    E newInstance(String id);
}
