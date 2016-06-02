package com.dmc.d1.cqrs.test.event;

import com.dmc.d1.cqrs.event.AggregateEvent;
import com.dmc.d1.cqrs.test.domain.MyId;
import com.dmc.d1.cqrs.test.domain.MyNestedId;

/**
 * Created by davidclelland on 16/05/2016.
 */
public class TriggeringExceptionInNestedAggregateEvent implements AggregateEvent {
    private final static String CLASS_NAME = TriggeringExceptionInNestedAggregateEvent.class.getName();
    private final MyId id;
    private final MyNestedId nestedId;

    private final String str;

    public TriggeringExceptionInNestedAggregateEvent(MyId id, MyNestedId nestedId, String str){
        this.id = id;
        this.nestedId = nestedId;
        this.str = str;

    }

    @Override
    public String getAggregateId() {
        return id.toString();
    }


    public MyId getId() {
        return id;
    }

    public MyNestedId getNestedId() {
        return nestedId;
    }

    @Override
    public String getClassName() {
        return CLASS_NAME;
    }

    public String getStr() {
        return str;
    }
}