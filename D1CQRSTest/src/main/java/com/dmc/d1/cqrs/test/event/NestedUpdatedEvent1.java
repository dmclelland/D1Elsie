package com.dmc.d1.cqrs.test.event;

import com.dmc.d1.cqrs.event.AggregateEvent;
import com.dmc.d1.cqrs.test.domain.MyId;
import com.dmc.d1.cqrs.test.domain.MyNestedId;

/**
 * Created by davidclelland on 16/05/2016.
 */
public class NestedUpdatedEvent1 implements AggregateEvent {
    private final static String CLASS_NAME = NestedUpdatedEvent1.class.getName();
    private final MyNestedId id;
    private final String str;

    public NestedUpdatedEvent1(MyNestedId id, String str){
        this.id = id;
        this.str = str;

    }

    @Override
    public String getAggregateId() {
        return id.toString();
    }

    @Override
    public String getClassName() {
        return CLASS_NAME;
    }

    public String getStr() {
        return str;
    }
}