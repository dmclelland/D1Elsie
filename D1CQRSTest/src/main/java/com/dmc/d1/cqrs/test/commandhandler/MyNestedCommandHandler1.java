package com.dmc.d1.cqrs.test.commandhandler;

import com.dmc.d1.cqrs.AggregateRepository;
import com.dmc.d1.cqrs.annotations.CommandHandler;
import com.dmc.d1.cqrs.AbstractCommandHandler;
import com.dmc.d1.cqrs.test.command.CreateNestedAggregate1Command;
import com.dmc.d1.cqrs.test.command.ExceptionTriggeringNestedAggregateCommand;
import com.dmc.d1.cqrs.test.domain.NestedAggregate1;

/**
 * Created By davidclelland on 31/05/2016.
 */
public class MyNestedCommandHandler1  extends AbstractCommandHandler<NestedAggregate1> {

    public MyNestedCommandHandler1(AggregateRepository<NestedAggregate1> repository) {
        super(repository);
    }

    @CommandHandler
    public void handle(CreateNestedAggregate1Command command) {
        NestedAggregate1 aggregate = new NestedAggregate1(command.getId());
        createAggregate(aggregate);
        aggregate.doSomething(command.getStr());
    }

    @CommandHandler
    public void handle(ExceptionTriggeringNestedAggregateCommand command) {
        NestedAggregate1 aggregate = getAggregate(command.getAggregateId());
        aggregate.doSomethingCausingError(command.getStr());
    }
}