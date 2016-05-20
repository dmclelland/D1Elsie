package com.dmc.d1.cqrs.testdomain.event;

import com.dmc.d1.cqrs.event.AggregateEvent;
import com.dmc.d1.domain.Id;

/**
 * Created by davidclelland on 16/05/2016.
 */
public class IntUpdatedEvent2 implements AggregateEvent {
    private final Id id;
    private final int i;

    private final static String SIMPLE_CLASS_NAME = IntUpdatedEvent2.class.getSimpleName();

    public IntUpdatedEvent2(Id id, int i){
        this.id = id;
        this.i = i;

    }

    @Override
    public Id getId() {
        return id;
    }

    @Override
    public String getSimpleClassName() {
        return SIMPLE_CLASS_NAME;
    }

    public int getI() {
        return i;
    }
}