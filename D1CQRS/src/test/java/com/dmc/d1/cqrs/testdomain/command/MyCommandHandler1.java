package com.dmc.d1.cqrs.testdomain.command;

import com.dmc.d1.cqrs.AggregateRepository;
import com.dmc.d1.cqrs.command.AnnotatedMethodInvokerStrategy;
import com.dmc.d1.cqrs.testdomain.Aggregate1;
import com.dmc.d1.cqrs.testdomain.MyId;
import com.dmc.d1.cqrs.annotations.CommandHandler;
import com.dmc.d1.cqrs.command.AbstractCommandHandler;

/**
 * Created by davidclelland on 17/05/2016.
 */
public class MyCommandHandler1 extends AbstractCommandHandler<MyId, Aggregate1> {

    public MyCommandHandler1(AggregateRepository repository, AnnotatedMethodInvokerStrategy strategy) {
        super(repository, strategy);
    }

    @CommandHandler
    public void handle(CreateAggregate1Command command) {

        Aggregate1 aggregate = new Aggregate1(command.getAggregateId());
        createAggregate(aggregate);

        aggregate.doSomething(command.getI1(), command.getI2());
    }

    @CommandHandler
    public void handle(UpdateAggregate1Command command) {
        Aggregate1 aggregate = getAggregate(command.getAggregateId());
        aggregate.doSomething(command.getI(), command.getI2());
    }
}