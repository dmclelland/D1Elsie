package com.dmc.d1.cqrs.event;

/**
 * Created By davidclelland on 14/06/2016.
 */
public interface AggregateEvent {

    long getAggregateId();
    String getClassName();
    String getAggregateClassName();
    void setAggregateClassName(String aggregateClassName);
}
