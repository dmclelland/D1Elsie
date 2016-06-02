package com.dmc.d1.cqrs.test.event;

import com.dmc.d1.cqrs.event.AggregateEvent;
import com.dmc.d1.cqrs.test.domain.MyId;

/**
 * Created by davidclelland on 16/05/2016.
 */
public class TriggerExceptionEvent implements AggregateEvent {
    private final static String CLASS_NAME = TriggerExceptionEvent.class.getName();
    private final MyId id;

    public TriggerExceptionEvent(MyId id){
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