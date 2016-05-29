package com.dmc.d1.cqrs.testdomain.event;

import com.dmc.d1.cqrs.event.AggregateEvent;
import com.dmc.d1.cqrs.testdomain.MyId;
import com.dmc.d1.domain.Id;

/**
 * Created by davidclelland on 16/05/2016.
 */
public class StringUpdatedEvent2 implements AggregateEvent {
    private final static String SIMPLE_CLASS_NAME = StringUpdatedEvent2.class.getSimpleName();
    private final MyId id;
    private final String str;

    public StringUpdatedEvent2(MyId id, String str){
        this.id = id;
        this.str = str;

    }

    @Override
    public String getId() {
        return id.toString();
    }

    @Override
    public String getSimpleClassName() {
        return SIMPLE_CLASS_NAME;
    }

    public String getStr() {
        return str;
    }

}