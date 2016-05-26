package com.dmc.d1.algo;

import com.dmc.d1.algo.aggregate.WaveAggregate;
import com.dmc.d1.algo.command.WaveCommandHandler;
import com.dmc.d1.cqrs.AggregateEventStore;
import com.dmc.d1.cqrs.AggregateRepository;
import com.dmc.d1.cqrs.InMemoryAggregateEventStore;
import com.dmc.d1.cqrs.command.*;
import com.dmc.d1.domain.WaveId;
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
    AggregateRepository<WaveId, WaveAggregate> waveAggregateRepository() {
        return new AggregateRepository<>(aggregateEventStore());
    }

    @Bean
    WaveCommandHandler waveCommandHandler(AggregateRepository<WaveId, WaveAggregate> waveAggregateRepository) {
        return new WaveCommandHandler(waveAggregateRepository, AnnotatedMethodInvokerStrategy.GENERATED);
    }

    @Bean
    List<? super AbstractCommandHandler> commandHandlers(WaveCommandHandler waveCommandHandler) {
        List<? super AbstractCommandHandler> lst = new ArrayList<>();
        lst.add(waveCommandHandler);
        return lst;
    }

    @Bean
    CommandBus commandBus(List<? extends AbstractCommandHandler> commandHandlers) {
        return new SimpleCommandBus(commandHandlers);
    }
}