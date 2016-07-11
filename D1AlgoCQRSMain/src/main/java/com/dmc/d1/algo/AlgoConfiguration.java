package com.dmc.d1.algo;

import com.dmc.d1.algo.aggregate.Basket;
import com.dmc.d1.algo.aggregate.BasketService;
import com.dmc.d1.algo.aggregate.PairsAggregate;
import com.dmc.d1.algo.aggregate.WaveAggregate;
import com.dmc.d1.algo.commandhandler.PairsCommandHandler;
import com.dmc.d1.algo.commandhandler.WaveCommandHandler;
import com.dmc.d1.algo.event.AlgoAggregateInitialisedEventBuilder;
import com.dmc.d1.cqrs.AbstractCommandHandler;
import com.dmc.d1.cqrs.Aggregate;
import com.dmc.d1.cqrs.AggregateRepository;
import com.dmc.d1.cqrs.command.CommandBus;
import com.dmc.d1.cqrs.command.SimpleCommandBus;
import com.dmc.d1.cqrs.event.AggregateInitialisedEvent;
import com.dmc.d1.cqrs.event.SimpleEventBus;
import com.dmc.d1.cqrs.event.store.AggregateEventStore;
import com.dmc.d1.cqrs.event.store.ChronicleAggregateEventStore;
import com.dmc.d1.cqrs.event.store.InMemoryAggregateEventStore;
import com.dmc.d1.domain.InstrumentId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Created by davidclelland on 18/05/2016.
 */
@Configuration
public class AlgoConfiguration {

    public static final String getChroniclePath() {
        return System.getProperty("java.io.tmpdir") + "/d1-events-" + System.currentTimeMillis();
    }


    @Bean
    SimpleEventBus<com.dmc.d1.cqrs.event.AbstractEventHandler> eventBus() {
        return new SimpleEventBus<>();
    }

    @Bean
    AggregateEventStore aggregateEventStore() {
        try {
            return new ChronicleAggregateEventStore(getChroniclePath() );
        } catch (IOException e) {
            throw new RuntimeException("Unable to configure chronicle event store",e);
        }
    }


    Function<String, AggregateInitialisedEvent> initialisationEventFactory =
            (ID) -> AlgoAggregateInitialisedEventBuilder.startBuilding(ID).buildJournalable();



    @Bean
    BasketService basketService(){
        return new BasketService() {
            @Override
            public Basket createBasket(InstrumentId id, int qty) {
                return new Basket();
            }
        };
    }

    @Bean
    WaveAggregate.WaveSupplier waveSupplier(BasketService basketService){
        return new WaveAggregate.WaveSupplier(basketService);

    }


    @Bean
    AggregateRepository<WaveAggregate> waveAggregateRepository(WaveAggregate.WaveSupplier waveSupplier)
    {
        return new AggregateRepository<>(aggregateEventStore(), WaveAggregate.class, eventBus(), waveSupplier,
                initialisationEventFactory
        );
    }

    @Bean
    AggregateRepository<PairsAggregate> pairAggregateRepository() {
        return new AggregateRepository<>(aggregateEventStore(), PairsAggregate.class, eventBus(), PairsAggregate.newInstanceFactory(),
                initialisationEventFactory
        );
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