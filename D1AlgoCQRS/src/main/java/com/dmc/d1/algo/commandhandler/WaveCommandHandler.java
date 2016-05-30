package com.dmc.d1.algo.commandhandler;


import com.dmc.d1.algo.aggregate.WaveAggregate;
import com.dmc.d1.algo.command.CancelWaveCommand;
import com.dmc.d1.algo.command.CreateWaveCommand;
import com.dmc.d1.algo.command.PauseWaveCommand;
import com.dmc.d1.algo.command.ResumeWaveCommand;
import com.dmc.d1.cqrs.AggregateRepository;
import com.dmc.d1.cqrs.annotations.CommandHandler;
import com.dmc.d1.cqrs.command.AbstractCommandHandler;
import com.dmc.d1.cqrs.AnnotatedMethodInvokerStrategy;

/**
 * Created by davidclelland on 18/05/2016.
 */
public class WaveCommandHandler extends AbstractCommandHandler<WaveAggregate> {

    public WaveCommandHandler(AggregateRepository<WaveAggregate> repository, AnnotatedMethodInvokerStrategy strategy) {
        super(repository, strategy);

    }

    @CommandHandler
    public void handle(CreateWaveCommand command) {
        WaveAggregate aggregate = new WaveAggregate(command.getWaveId());
        createAggregate(aggregate);
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
