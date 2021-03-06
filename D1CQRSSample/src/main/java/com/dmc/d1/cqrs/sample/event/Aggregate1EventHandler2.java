package com.dmc.d1.cqrs.sample.event;

import com.dmc.d1.cqrs.annotations.EventHandler;
import com.dmc.d1.cqrs.event.AbstractEventHandler;
import com.dmc.d1.sample.event.HandledByExternalHandlersEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Created By davidclelland on 31/05/2016.
 */
public class Aggregate1EventHandler2 extends AbstractEventHandler {

    private final Map<Long, String> map = new HashMap<>();

    @EventHandler
    public void handle(HandledByExternalHandlersEvent event) {
        map.put(event.getAggregateId(), event.getStr());
    }


    public String getString(long id) {
        return map.get(id);
    }

}
