package com.dmc.d1.cqrs.test.commandhandler;

import com.dmc.d1.cqrs.AbstractCommandHandler;
import com.dmc.d1.cqrs.AggregateRepository;
import com.dmc.d1.cqrs.AnnotatedCommandHandlerInvoker;
import com.dmc.d1.cqrs.annotations.CommandHandler;
import com.dmc.d1.cqrs.test.aggregate.Aggregate1;
import com.dmc.d1.cqrs.test.aggregate.ComplexAggregate;
import com.dmc.d1.cqrs.test.command.*;

/**
 * Created by davidclelland on 17/05/2016.
 */
public class ComplexCommandHandler extends AbstractCommandHandler<ComplexAggregate> {


    public ComplexCommandHandler(AggregateRepository repository) {
        super(repository);

    }


    public ComplexCommandHandler(AggregateRepository repository, AnnotatedCommandHandlerInvoker commandHandlerInvoker) {
        super(repository, commandHandlerInvoker);
    }


    @CommandHandler
    public void handle(CreateComplexAggregateCommand command) {

        ComplexAggregate aggregate = initialiseAggregate(command.getId().asString());
        aggregate.createBasket(command.getBasket());
    }



}