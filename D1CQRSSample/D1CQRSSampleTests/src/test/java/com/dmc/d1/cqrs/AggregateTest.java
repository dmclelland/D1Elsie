package com.dmc.d1.cqrs;


import com.dmc.d1.cqrs.event.AggregateInitialisedEvent;
import com.dmc.d1.cqrs.event.SimpleEventBus;
import com.dmc.d1.cqrs.event.store.AggregateEventStore;
import com.dmc.d1.cqrs.event.store.InMemoryAggregateEventStore;
import com.dmc.d1.cqrs.test.aggregate.Aggregate1;
import com.dmc.d1.cqrs.test.aggregate.Aggregate2;
import com.dmc.d1.cqrs.test.domain.MyId;
import com.dmc.d1.test.event.TestAggregateInitialisedEventBuilder;
import org.junit.Test;

import java.util.function.Function;

import static org.junit.Assert.assertEquals;

/**
 * Created by davidclelland on 16/05/2016.
 */
public class AggregateTest {

    SimpleEventBus bus = new SimpleEventBus();

    AggregateEventStore eventStore = new InMemoryAggregateEventStore();
    //InitialisationEventFactory initialisationEventFactory = Configuration.initialisationEventFactoryBasic();

    Function<String, AggregateInitialisedEvent> initialisationFactory =
            (ID) -> TestAggregateInitialisedEventBuilder.startBuilding(ID).buildJournalable();

    AggregateRepository<Aggregate1> aggregate1Repo =
            new AggregateRepository(eventStore, Aggregate1.class, bus, Aggregate1.newInstanceFactory(), initialisationFactory);
    AggregateRepository<Aggregate2> aggregate2Repo =
            new AggregateRepository(eventStore, Aggregate2.class, bus, Aggregate2.newInstanceFactory(), initialisationFactory);

    MyId id1 = MyId.from("testId1");
    MyId id2 = MyId.from("testId1");



    @Test
    public void testAggregate() {

        Aggregate1 aggregate1 = aggregate1Repo.create(id1.asString());
        Aggregate2 aggregate2 = aggregate2Repo.create(id2.asString());

        aggregate1.doSomething(5, 12);
        aggregate2.doSomething("Hello", "Goodbye");

        assertEquals(5, aggregate1.getI1());
        assertEquals(12, aggregate1.getI2());

        assertEquals("Hello", aggregate2.getS1());
        assertEquals("Goodbye", aggregate2.getS2());
    }
}
