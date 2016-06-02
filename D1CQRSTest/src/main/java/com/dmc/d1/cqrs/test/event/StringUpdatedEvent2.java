package com.dmc.d1.cqrs.test.event;

import com.dmc.d1.cqrs.test.domain.MyId;
import com.dmc.d1.cqrs.event.AggregateEvent;

/**
 * Created by davidclelland on 16/05/2016.
 */
public class StringUpdatedEvent2 implements AggregateEvent {
    private final static String CLASS_NAME = StringUpdatedEvent2.class.getName();
    private final MyId id;
    private final String str;

    public StringUpdatedEvent2(MyId id, String str){
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