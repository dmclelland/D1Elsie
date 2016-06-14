package com.dmc.d1.cqrs.test.commandhandler;

import com.dmc.d1.cqrs.AbstractCommandHandler;
import com.dmc.d1.cqrs.AggregateRepository;
import com.dmc.d1.cqrs.AnnotatedCommandHandlerInvoker;
import com.dmc.d1.cqrs.annotations.CommandHandler;
import com.dmc.d1.cqrs.test.aggregate.Aggregate1;
import com.dmc.d1.cqrs.test.command.CreateAggregate1Command;
import com.dmc.d1.cqrs.test.command.NestedExceptionTriggeringAggregate1Command;
import com.dmc.d1.cqrs.test.command.UpdateAggregate1Command;
import com.dmc.d1.cqrs.test.command.UpdateAggregate1Command2;

/**
 * Created by davidclelland on 17/05/2016.
 */
public class MyCommandHandler1 extends AbstractCommandHandler<Aggregate1> {


    public MyCommandHandler1(AggregateRepository repository) {
        super(repository);

    }


    public MyCommandHandler1(AggregateRepository repository, AnnotatedCommandHandlerInvoker commandHandlerInvoker) {
        super(repository, commandHandlerInvoker);
    }


    @CommandHandler
    public void handle(CreateAggregate1Command command) {

        Aggregate1 aggregate = initialiseAggregate(command.getId().asString());
        aggregate.doSomething(command.getI1(), command.getI2());
    }

    @CommandHandler
    public void handle(UpdateAggregate1Command command) {
        Aggregate1 aggregate = getAggregate(command.getAggregateId());
        aggregate.doSomething(command.getI(), command.getI2());
    }

    @CommandHandler
    public void handle(UpdateAggregate1Command2 command) {
        Aggregate1 aggregate = getAggregate(command.getAggregateId());
        aggregate.doSomething2(command.getStr());
    }

    @CommandHandler
    public void handle(NestedExceptionTriggeringAggregate1Command command) {
        Aggregate1 aggregate = getAggregate(command.getAggregateId());
        aggregate.triggerExceptionInNestedAggregate(command.getStr());
    }

}