package com.dmc.d1.cqrs.testdomain.command;

import com.dmc.d1.cqrs.AggregateRepository;
import com.dmc.d1.cqrs.testdomain.Aggregate2;
import com.dmc.d1.cqrs.testdomain.MyId;
import com.dmc.d1.cqrs.annotations.CommandHandler;
import com.dmc.d1.cqrs.command.AbstractCommandHandler;

/**
 * Created by davidclelland on 17/05/2016.
 */
public class MyCommandHandler2 extends AbstractCommandHandler<MyId, Aggregate2>{

    public MyCommandHandler2(AggregateRepository repository) {
        super(repository);
    }

    @CommandHandler
    public void handle(CreateAggregate2Command command){

        Aggregate2 aggregate = new Aggregate2(command.getAggregateId());
        createAggregate(aggregate);

        aggregate.doSomething(command.getStr1(), command.getStr2());
    }

    @CommandHandler
    public void handle(UpdateAggregate2Command command){
        Aggregate2 aggregate = getAggregate(command.getAggregateId());
        aggregate.doSomething(command.getStr1(), command.getStr2());

        if(true)
            throw new RuntimeException("Unexpected");
    }
}