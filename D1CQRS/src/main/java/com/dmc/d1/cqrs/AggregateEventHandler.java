package com.dmc.d1.cqrs;

import com.dmc.d1.cqrs.event.AggregateEvent;

/**
 * Created by davidclelland on 16/05/2016.
 */
interface AggregateEventHandler {
    void invoke(AggregateEvent event, Aggregate aggregate);
}
