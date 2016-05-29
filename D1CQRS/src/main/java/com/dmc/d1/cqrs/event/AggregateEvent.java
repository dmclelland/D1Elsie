package com.dmc.d1.cqrs.event;

/**
 * Created by davidclelland on 16/05/2016.
 */
public interface AggregateEvent {
    String getId();
    String getSimpleClassName();

}
