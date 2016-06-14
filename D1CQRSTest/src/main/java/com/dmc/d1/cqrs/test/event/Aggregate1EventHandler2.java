package com.dmc.d1.cqrs.test.event;

import com.dmc.d1.algo.event.HandledByExternalHandlersEvent;
import com.dmc.d1.cqrs.annotations.EventHandler;
import com.dmc.d1.cqrs.test.domain.MyId;
import com.dmc.d1.cqrs.event.AbstractEventHandler;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created By davidclelland on 31/05/2016.
 */
public class Aggregate1EventHandler2 extends AbstractEventHandler {

    private final Map<String, String> map = new HashMap<>();

    @EventHandler
    public void handle(HandledByExternalHandlersEvent event){
        map.put(event.getAggregateId(), event.getStr());
    }


    public String getString(String id){
        return map.get(id);
    }

}
