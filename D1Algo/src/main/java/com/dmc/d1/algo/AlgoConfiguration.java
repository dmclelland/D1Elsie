package com.dmc.d1.algo;

import com.dmc.d1.algo.aggregate.PairsAggregate;
import com.dmc.d1.algo.aggregate.WaveAggregate;
import com.dmc.d1.algo.commandhandler.PairsCommandHandler;
import com.dmc.d1.algo.commandhandler.WaveCommandHandler;
import com.dmc.d1.cqrs.*;
import com.dmc.d1.cqrs.command.AbstractCommandHandler;
import com.dmc.d1.cqrs.command.CommandBus;
import com.dmc.d1.cqrs.command.SimpleCommandBus;
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
    AggregateEventStore aggregateEventStore() {
        return new InMemoryAggregateEventStore();
    }

    @Bean
    AggregateRepository<WaveAggregate> waveAggregateRepository() {
        return new AggregateRepository<>(aggregateEventStore(), WaveAggregate.class, AnnotatedMethodInvokerStrategy.GENERATED);
    }

    @Bean
    AggregateRepository<PairsAggregate> pairAggregateRepository() {
        return new AggregateRepository<>(aggregateEventStore(), PairsAggregate.class, AnnotatedMethodInvokerStrategy.GENERATED);
    }

    @Bean
    WaveCommandHandler waveCommandHandler(AggregateRepository<WaveAggregate> waveAggregateRepository) {
        return new WaveCommandHandler(waveAggregateRepository, AnnotatedMethodInvokerStrategy.GENERATED);
    }

    @Bean
    PairsCommandHandler pairsCommandHandler(AggregateRepository<PairsAggregate> pairsAggregateRepository) {
        return new PairsCommandHandler(pairsAggregateRepository, AnnotatedMethodInvokerStrategy.GENERATED);
    }

    @Bean
    List<? super AbstractCommandHandler<? extends Aggregate>> commandHandlers(WaveCommandHandler waveCommandHandler, PairsCommandHandler pairsCommandHandler) {

        List<? super AbstractCommandHandler<? extends Aggregate>> lst = new ArrayList<>();
        lst.add(waveCommandHandler);
        lst.add(pairsCommandHandler);

        return lst;
    }

    @Bean
    CommandBus commandBus(List<? extends AbstractCommandHandler<? extends Aggregate>>  commandHandlers) {
        @SuppressWarnings("unchecked")
        SimpleCommandBus bus = new SimpleCommandBus(commandHandlers);
        return bus;
    }


}