package com.dmc.d1.algo;

import com.dmc.d1.algo.aggregate.PairsAggregate;
import com.dmc.d1.algo.aggregate.WaveAggregate;
import com.dmc.d1.algo.commandhandler.PairsCommandHandler;
import com.dmc.d1.algo.commandhandler.WaveCommandHandler;
import com.dmc.d1.cqrs.AbstractCommandHandler;
import com.dmc.d1.cqrs.Aggregate;
import com.dmc.d1.cqrs.AggregateRepository;
import com.dmc.d1.cqrs.command.CommandBus;
import com.dmc.d1.cqrs.command.SimpleCommandBus;
import com.dmc.d1.cqrs.event.SimpleEventBus;
import com.dmc.d1.cqrs.event.store.AggregateEventStore;
import com.dmc.d1.cqrs.event.store.InMemoryAggregateEventStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by davidclelland on 18/05/2016.
 */
@Configuration
public class AlgoConfiguration {

    @Bean
    SimpleEventBus<com.dmc.d1.cqrs.event.AbstractEventHandler> eventBus() {
        return new SimpleEventBus<>();
    }

    @Bean
    AggregateEventStore aggregateEventStore() {
        return new InMemoryAggregateEventStore();
    }


    @Bean
    AggregateRepository<WaveAggregate> waveAggregateRepository() {
        return new AggregateRepository<>(aggregateEventStore(), WaveAggregate.class, eventBus(), WaveAggregate.newInstanceFactory(),
                s -> null);
    }

    @Bean
    AggregateRepository<PairsAggregate> pairAggregateRepository() {
        return new AggregateRepository<>(aggregateEventStore(), PairsAggregate.class, eventBus(), PairsAggregate.newInstanceFactory(), s -> null);
    }

    @Bean
    WaveCommandHandler waveCommandHandler(AggregateRepository<WaveAggregate> waveAggregateRepository) {
        return new WaveCommandHandler(waveAggregateRepository);
    }

    @Bean
    PairsCommandHandler pairsCommandHandler(AggregateRepository<PairsAggregate> pairsAggregateRepository) {
        return new PairsCommandHandler(pairsAggregateRepository);
    }


    @Bean
    List<? super AbstractCommandHandler<? extends Aggregate>> commandHandlers(WaveCommandHandler waveCommandHandler, PairsCommandHandler pairsCommandHandler) {

        List<? super AbstractCommandHandler<? extends Aggregate>> lst = new ArrayList<>();
        lst.add(waveCommandHandler);
        lst.add(pairsCommandHandler);

        return lst;
    }


    @Bean
    CommandBus commandBus(List<? extends AbstractCommandHandler<? extends Aggregate>> commandHandlers) {
        SimpleCommandBus bus = new SimpleCommandBus(commandHandlers);
        return bus;
    }

}