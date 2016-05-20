package com.dmc.d1.algo.command;


import com.dmc.d1.algo.aggregate.WaveAggregate;
import com.dmc.d1.cqrs.AggregateRepository;
import com.dmc.d1.cqrs.annotations.CommandHandler;
import com.dmc.d1.cqrs.command.AbstractCommandHandler;
import com.dmc.d1.domain.WaveId;

/**
 * Created by davidclelland on 18/05/2016.
 */
public class WaveCommandHandler extends AbstractCommandHandler<WaveId, WaveAggregate> {

    public WaveCommandHandler(AggregateRepository repository) {
        super(repository);
    }

    @CommandHandler
    public void handle(CreateWaveCommand command) {
        WaveAggregate aggregate = new WaveAggregate(command.getAggregateId());
        createAggregate(aggregate);
    }


    @CommandHandler
    public void handle(CancelWaveCommand command) {

    }

    @CommandHandler
    public void handle(PauseWaveCommand command) {

    }

    @CommandHandler
    public void handle(ResumeWaveCommand command) {

    }

}
