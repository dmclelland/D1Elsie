package com.dmc.d1.algo.commandhandler;


import com.dmc.d1.algo.aggregate.PairsAggregate;
import com.dmc.d1.algo.command.CreatePairCommand;
import com.dmc.d1.cqrs.AggregateRepository;
import com.dmc.d1.cqrs.annotations.CommandHandler;
import com.dmc.d1.cqrs.AbstractCommandHandler;

/**
 * Created by davidclelland on 18/05/2016.
 */
public class PairsCommandHandler extends AbstractCommandHandler<PairsAggregate> {


    public PairsCommandHandler(AggregateRepository<PairsAggregate> repository) {
        super(repository);
    }



    @CommandHandler
    public void handle(CreatePairCommand command) {
         PairsAggregate aggregate = initialiseAggregate(command.getPairId().asString());
    }
}
