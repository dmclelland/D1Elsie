package com.dmc.d1.algo.commandhandler;


import com.dmc.d1.algo.aggregate.BasketService;
import com.dmc.d1.algo.aggregate.WaveAggregate;
import com.dmc.d1.algo.command.CancelWaveCommand;
import com.dmc.d1.algo.command.CreateWaveCommand;
import com.dmc.d1.algo.command.PauseWaveCommand;
import com.dmc.d1.algo.command.ResumeWaveCommand;
import com.dmc.d1.cqrs.AbstractCommandHandler;
import com.dmc.d1.cqrs.AggregateRepository;
import com.dmc.d1.cqrs.annotations.CommandHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by davidclelland on 18/05/2016.
 */
public class WaveCommandHandler extends AbstractCommandHandler<WaveAggregate> {
    public WaveCommandHandler(AggregateRepository<WaveAggregate> repository) {
        super(repository);
    }

    @Autowired
    private BasketService basketService;

    @CommandHandler
    public void handle(CreateWaveCommand command, WaveAggregate aggregate) {

        aggregate.setServices(basketService);
        //aggregate.createWave(command.getWaveId(),);
    }

    @CommandHandler
    public void handle(CancelWaveCommand command, WaveAggregate aggregate) {

    }

    @CommandHandler
    public void handle(PauseWaveCommand command, WaveAggregate aggregate) {
    }

    @CommandHandler
    public void handle(ResumeWaveCommand command, WaveAggregate aggregate) {
    }
}
