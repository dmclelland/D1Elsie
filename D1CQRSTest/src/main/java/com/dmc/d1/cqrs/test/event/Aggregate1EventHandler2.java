package com.dmc.d1.cqrs.test.event;

import com.dmc.d1.cqrs.annotations.EventHandler;
import com.dmc.d1.cqrs.command.CommandBus;
import com.dmc.d1.cqrs.event.AbstractEventHandler;
import com.dmc.d1.cqrs.test.command.CreateNestedAggregate1Command;
import com.dmc.d1.cqrs.test.domain.MyId;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created By davidclelland on 31/05/2016.
 */
public class Aggregate1EventHandler2 extends AbstractEventHandler{

    private final Map<MyId, String> map = new HashMap<>();


    @EventHandler
    public void handle(StringUpdatedEvent3 event){
        map.put(event.getId(), event.getStr());
    }


    public String getString(MyId id){
        return map.get(id);
    }

}
