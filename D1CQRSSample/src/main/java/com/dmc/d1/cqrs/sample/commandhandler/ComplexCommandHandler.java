package com.dmc.d1.cqrs.sample.commandhandler;

import com.dmc.d1.cqrs.AbstractCommandHandler;
import com.dmc.d1.cqrs.AggregateRepository;
import com.dmc.d1.cqrs.AnnotatedCommandHandlerInvoker;
import com.dmc.d1.cqrs.annotations.CommandHandler;
import com.dmc.d1.cqrs.sample.aggregate.ComplexAggregate;
import com.dmc.d1.cqrs.sample.command.CreateComplexAggregateCommand;

import static com.google.common.base.Preconditions.checkNotNull;

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
    public void handle(CreateComplexAggregateCommand command, ComplexAggregate aggregate) {

        aggregate.createBasket(command.getBasket());
    }



}