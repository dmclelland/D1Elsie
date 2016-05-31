package com.dmc.d1.cqrs.test.event;

import com.dmc.d1.cqrs.event.AggregateEvent;
import com.dmc.d1.cqrs.test.domain.MyId;
import com.dmc.d1.domain.Id;

/**
 * Created by davidclelland on 16/05/2016.
 */
public class IntUpdatedEvent2 implements AggregateEvent {
    private final static String CLASS_NAME = IntUpdatedEvent2.class.getName();
    private final MyId id;
    private final int i;

    public IntUpdatedEvent2(MyId id, int i){
        this.id = id;
        this.i = i;

    }

    @Override
    public String getAggregateId() {
        return id.toString();
    }

    @Override
    public String getClassName() {
        return CLASS_NAME;
    }

    public int getI() {
        return i;
    }
}