package com.dmc.d1.cqrs.test.commandhandler;

import com.dmc.d1.cqrs.AbstractCommandHandler;
import com.dmc.d1.cqrs.AggregateRepository;
import com.dmc.d1.cqrs.AnnotatedCommandHandlerInvoker;
import com.dmc.d1.cqrs.annotations.CommandHandler;
import com.dmc.d1.cqrs.test.aggregate.Aggregate2;
import com.dmc.d1.cqrs.test.command.CreateAggregate2Command;
import com.dmc.d1.cqrs.test.command.ExceptionTriggeringAggregate2Command;
import com.dmc.d1.cqrs.test.command.UpdateAggregate2Command;

/**
 * Created by davidclelland on 17/05/2016.
 */
public class MyCommandHandler2 extends AbstractCommandHandler<Aggregate2> {


    public MyCommandHandler2(AggregateRepository repository) {
        super(repository);
    }

    public MyCommandHandler2(AggregateRepository repository, AnnotatedCommandHandlerInvoker commandHandlerInvoker) {
        super(repository, commandHandlerInvoker);
    }


    @CommandHandler
    public void handle(CreateAggregate2Command command) {
        Aggregate2 aggregate = initialiseAggregate(command.getId().asString());
        aggregate.doSomething(command.getStr1(), command.getStr2());
    }

    @CommandHandler
    public void handle(UpdateAggregate2Command command) {
        Aggregate2 aggregate = getAggregate(command.getAggregateId());
        aggregate.doSomething(command.getStr1(), command.getStr2());
    }


    @CommandHandler
    public void handle(ExceptionTriggeringAggregate2Command command) {
        Aggregate2 aggregate = getAggregate(command.getAggregateId());
        aggregate.doSomethingWhichCausesException(command.getStr1(), command.getStr2());
    }
}