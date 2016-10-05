package com.dmc.d1.cqrs.sample.commandhandler;

import com.dmc.d1.cqrs.AbstractCommandHandler;
import com.dmc.d1.cqrs.AggregateRepository;
import com.dmc.d1.cqrs.annotations.CommandHandler;
import com.dmc.d1.cqrs.sample.aggregate.ComplexMutableAggregate;
import com.dmc.d1.cqrs.sample.command.CreateMutableComplexAggregateCommand;
import com.dmc.d1.cqrs.sample.command.UpdateComplexAggregateCommand;
import com.dmc.d1.cqrs.sample.command.UpdateComplexAggregateWithDeterministicExceptionCommand;

/**
 * Created by davidclelland on 17/05/2016.
 */
public class ComplexMutableCommandHandler extends AbstractCommandHandler<ComplexMutableAggregate> {


    public ComplexMutableCommandHandler(AggregateRepository repository) {
        super(repository);

    }

    @CommandHandler
    public void handle(CreateMutableComplexAggregateCommand command, ComplexMutableAggregate aggregate) {
        aggregate.createBasket2(command.getBasket());
    }

    @CommandHandler
    public void handle(UpdateComplexAggregateCommand command, ComplexMutableAggregate aggregate) {
        aggregate.updateBasketConstituent(command.getRic(), command.getAdjustedShares());
    }

    @CommandHandler
    public void handle(UpdateComplexAggregateWithDeterministicExceptionCommand command, ComplexMutableAggregate aggregate) {
        aggregate.updateBasketConstituentWithDeterministicException(command.getRic(), command.getAdjustedShares());
    }

}