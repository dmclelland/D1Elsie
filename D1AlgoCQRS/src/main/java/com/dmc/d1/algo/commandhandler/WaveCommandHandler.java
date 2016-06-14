package com.dmc.d1.algo.commandhandler;


import com.dmc.d1.algo.aggregate.WaveAggregate;
import com.dmc.d1.algo.command.CancelWaveCommand;
import com.dmc.d1.algo.command.CreateWaveCommand;
import com.dmc.d1.algo.command.PauseWaveCommand;
import com.dmc.d1.algo.command.ResumeWaveCommand;
import com.dmc.d1.cqrs.AbstractCommandHandler;
import com.dmc.d1.cqrs.AggregateRepository;
import com.dmc.d1.cqrs.annotations.CommandHandler;

/**
 * Created by davidclelland on 18/05/2016.
 */
public class WaveCommandHandler extends AbstractCommandHandler<WaveAggregate> {
    public WaveCommandHandler(AggregateRepository<WaveAggregate> repository) {
        super(repository);
    }

    @CommandHandler
    public void handle(CreateWaveCommand command) {

        WaveAggregate aggregate = initialiseAggregate(command.getWaveId().toString());
    }

    @CommandHandler
    public void handle(CancelWaveCommand command) {
        getAggregate(command.getAggregateId());
    }

    @CommandHandler
    public void handle(PauseWaveCommand command) {
    }

    @CommandHandler
    public void handle(ResumeWaveCommand command) {
    }
}
