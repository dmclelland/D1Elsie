package com.dmc.d1.cqrs.test.commandhandler;

import com.dmc.d1.cqrs.AggregateRepository;
import com.dmc.d1.cqrs.annotations.CommandHandler;
import com.dmc.d1.cqrs.command.AbstractCommandHandler;
import com.dmc.d1.cqrs.test.command.CreateAggregate2Command;
import com.dmc.d1.cqrs.test.command.UpdateAggregate2Command;
import com.dmc.d1.cqrs.test.domain.Aggregate2;

/**
 * Created by davidclelland on 17/05/2016.
 */
public class MyCommandHandler2 extends AbstractCommandHandler<Aggregate2> {

    public MyCommandHandler2(AggregateRepository repository) {
        super(repository);
    }

    @CommandHandler
    public void handle(CreateAggregate2Command command) {

        Aggregate2 aggregate = new Aggregate2(command.getId());
        createAggregate(aggregate);

        aggregate.doSomething(command.getStr1(), command.getStr2());
    }

    @CommandHandler
    public void handle(UpdateAggregate2Command command) {
        Aggregate2 aggregate = getAggregate(command.getAggregateId());
        aggregate.doSomething(command.getStr1(), command.getStr2());

        if (true)
            throw new RuntimeException("Unexpected");
    }
}