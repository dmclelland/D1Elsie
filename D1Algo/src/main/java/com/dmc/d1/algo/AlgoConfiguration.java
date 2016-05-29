package com.dmc.d1.algo;

import com.dmc.d1.algo.aggregate.WaveAggregate;
import com.dmc.d1.algo.command.WaveCommandHandler;
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
        return new AggregateRepository<>(aggregateEventStore(),WaveAggregate.class, AnnotatedMethodInvokerStrategy.GENERATED);
    }

    @Bean
    WaveCommandHandler waveCommandHandler(AggregateRepository<WaveAggregate> waveAggregateRepository) {
        return new WaveCommandHandler(waveAggregateRepository, AnnotatedMethodInvokerStrategy.GENERATED);
    }

    @Bean
    List<? super AbstractCommandHandler<? extends Aggregate>> commandHandlers(WaveCommandHandler waveCommandHandler) {
        List<? super AbstractCommandHandler<? extends Aggregate>> lst = new ArrayList<>();
        lst.add(waveCommandHandler);

        return lst;
    }

    @Bean
    CommandBus commandBus(List<? extends AbstractCommandHandler<? extends Aggregate>> commandHandlers) {
        return new SimpleCommandBus(commandHandlers);
    }
}