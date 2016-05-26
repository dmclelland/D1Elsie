package com.dmc.d1.algo.command;


import com.dmc.d1.algo.aggregate.PairsAggregate;
import com.dmc.d1.cqrs.AggregateRepository;
import com.dmc.d1.cqrs.annotations.CommandHandler;
import com.dmc.d1.cqrs.command.AbstractCommandHandler;
import com.dmc.d1.cqrs.command.AnnotatedMethodInvokerStrategy;
import com.dmc.d1.domain.PairId;

/**
 * Created by davidclelland on 18/05/2016.
 */
public class PairsCommandHandler extends AbstractCommandHandler<PairId, PairsAggregate> {

    public PairsCommandHandler(AggregateRepository repository, AnnotatedMethodInvokerStrategy strategy) {
        super(repository, strategy);

    }

    @CommandHandler
    public void handle(CreatePairCommand command) {
        PairsAggregate aggregate = new PairsAggregate(command.getAggregateId());
        createAggregate(aggregate);
    }



}
