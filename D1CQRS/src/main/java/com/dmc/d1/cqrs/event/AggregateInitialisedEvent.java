package com.dmc.d1.cqrs.event;

/**
 * Created By davidclelland on 12/06/2016.
 */
public class AggregateInitialisedEvent extends AggregateEventAbstract {

    private final static String CLASS_NAME = AggregateInitialisedEvent.class.getName();

    public AggregateInitialisedEvent(String id) {
        setAggregateId(id);
        setClassName(CLASS_NAME);
    }


}