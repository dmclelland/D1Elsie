package com.dmc.d1.cqrs.testdomain.event;

import com.dmc.d1.cqrs.event.AggregateEvent;
import com.dmc.d1.cqrs.testdomain.MyId;
import com.dmc.d1.domain.Id;

/**
 * Created by davidclelland on 16/05/2016.
 */
public class IntUpdatedEvent1 implements AggregateEvent {
    private final static String SIMPLE_CLASS_NAME = IntUpdatedEvent1.class.getSimpleName();
    private final MyId id;
    private final int i;

    public IntUpdatedEvent1(MyId id, int i){
        this.id = id;
        this.i = i;

    }

    @Override
    public String getId() {
        return id.toString();
    }

    @Override
    public String getSimpleClassName() {
        return SIMPLE_CLASS_NAME;
    }

    public int getI() {
        return i;
    }
}