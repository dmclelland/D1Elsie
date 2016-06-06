package com.dmc.d1.cqrs.test.commandhandler;

import com.dmc.d1.algo.event.EventFactoryAbstract;
import com.dmc.d1.cqrs.AggregateRepository;
import com.dmc.d1.cqrs.annotations.CommandHandler;
import com.dmc.d1.cqrs.AbstractCommandHandler;
import com.dmc.d1.cqrs.test.command.CreateAggregate2Command;
import com.dmc.d1.cqrs.test.command.ExceptionTriggeringAggregate2Command;
import com.dmc.d1.cqrs.test.aggregate.Aggregate2;

/**
 * Created by davidclelland on 17/05/2016.
 */
public class MyCommandHandler2 extends AbstractCommandHandler<Aggregate2> {

    private final EventFactoryAbstract eventFactory;


    public MyCommandHandler2(AggregateRepository repository, EventFactoryAbstract eventFactory) {
        super(repository);
        this.eventFactory = eventFactory;
    }

    @CommandHandler
    public void handle(CreateAggregate2Command command) {
        Aggregate2 aggregate = new Aggregate2(command.getId(), eventFactory);
        initialiseAggregate(aggregate);
        aggregate.doSomething(command.getStr1(), command.getStr2());
    }

    @CommandHandler
    public void handle(ExceptionTriggeringAggregate2Command command) {
        Aggregate2 aggregate = getAggregate(command.getAggregateId());
        aggregate.doSomethingWhichCausesException(command.getStr1(), command.getStr2());
    }
}