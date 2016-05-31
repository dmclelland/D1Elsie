package com.dmc.d1.cqrs.event;

import com.dmc.d1.cqrs.Aggregate;
import com.dmc.d1.cqrs.command.AbstractCommandHandler;
import com.dmc.d1.cqrs.command.Command;

/**
 * Created by davidclelland on 18/05/2016.
 */
public interface AnnotatedEventHandlerInvoker<H extends AbstractEventHandler>{
    void invoke(AggregateEvent event, H eventHandler);
}
