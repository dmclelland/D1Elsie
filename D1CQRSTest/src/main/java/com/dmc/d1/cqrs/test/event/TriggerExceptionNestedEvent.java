package com.dmc.d1.cqrs.test.event;

import com.dmc.d1.cqrs.event.AggregateEvent;
import com.dmc.d1.cqrs.test.domain.MyId;
import com.dmc.d1.cqrs.test.domain.MyNestedId;

/**
 * Created by davidclelland on 16/05/2016.
 */
public class TriggerExceptionNestedEvent implements AggregateEvent {
    private final static String CLASS_NAME = TriggerExceptionNestedEvent.class.getName();
    private final MyNestedId id;

    public TriggerExceptionNestedEvent(MyNestedId id){
        this.id = id;
    }

    @Override
    public String getAggregateId() {
        return id.toString();
    }

    @Override
    public String getClassName() {
        return CLASS_NAME;
    }


}