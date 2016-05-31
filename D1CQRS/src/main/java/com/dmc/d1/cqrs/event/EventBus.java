package com.dmc.d1.cqrs.event;

import com.dmc.d1.cqrs.command.Command;

/**
 * Created by davidclelland on 16/05/2016.
 */
public interface EventBus {

    void publish(AggregateEvent event);
}
