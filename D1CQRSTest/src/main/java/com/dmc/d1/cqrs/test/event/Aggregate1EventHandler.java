package com.dmc.d1.cqrs.test.event;

import com.dmc.d1.cqrs.annotations.EventHandler;
import com.dmc.d1.cqrs.command.CommandBus;
import com.dmc.d1.cqrs.event.AbstractEventHandler;
import com.dmc.d1.cqrs.test.command.CreateNestedAggregate1Command;
import com.dmc.d1.cqrs.test.command.ExceptionTriggeringNestedAggregateCommand;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created By davidclelland on 31/05/2016.
 */
public class Aggregate1EventHandler extends AbstractEventHandler {

    private final CommandBus bus;

    public Aggregate1EventHandler(CommandBus bus){
        this.bus = checkNotNull(bus);
    }

    @EventHandler
    public void handle(HandledByExternalHandlersEvent event){
        bus.dispatch(new CreateNestedAggregate1Command(event.getNestedId(), event.getStr()));
    }

    @EventHandler
    public void handle(TriggeringExceptionInNestedAggregateEvent event){
        bus.dispatch(new ExceptionTriggeringNestedAggregateCommand(event.getNestedId(),event.getStr()));
    }
}
