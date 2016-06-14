package com.dmc.d1.cqrs.event;

/**
 * Created by davidclelland on 16/05/2016.
 */
public interface EventBus {

    void publish(AggregateEvent event);
}
