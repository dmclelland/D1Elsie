package com.dmc.d1.cqrs.testdomain.event;

import com.dmc.d1.cqrs.event.AggregateEvent;
import com.dmc.d1.domain.Id;

/**
 * Created by davidclelland on 16/05/2016.
 */
public class StringUpdatedEvent1 implements AggregateEvent {
    private final Id id;
    private final String str;

    private final static String SIMPLE_CLASS_NAME = StringUpdatedEvent1.class.getSimpleName();

    public StringUpdatedEvent1(Id id, String str){
        this.id = id;
        this.str = str;

    }

    @Override
    public Id getId() {
        return id;
    }

    @Override
    public String getSimpleClassName() {
        return SIMPLE_CLASS_NAME;
    }

    public String getStr() {
        return str;
    }
}