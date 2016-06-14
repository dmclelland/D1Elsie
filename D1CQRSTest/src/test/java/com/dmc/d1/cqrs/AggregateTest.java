package com.dmc.d1.cqrs;


import com.dmc.d1.algo.event.EventFactoryBasic;
import com.dmc.d1.cqrs.event.SimpleEventBus;
import com.dmc.d1.cqrs.event.store.AggregateEventStore;
import com.dmc.d1.cqrs.event.store.InMemoryAggregateEventStore;
import com.dmc.d1.cqrs.test.aggregate.Aggregate1;
import com.dmc.d1.cqrs.test.aggregate.Aggregate2;
import com.dmc.d1.cqrs.test.aggregate.AggregateFactoryImpl;
import com.dmc.d1.cqrs.test.domain.MyId;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by davidclelland on 16/05/2016.
 */
public class AggregateTest {

    SimpleEventBus bus = new SimpleEventBus();

    AggregateEventStore eventStore = new InMemoryAggregateEventStore();
    AggregateFactory aggregateFactory = new AggregateFactoryImpl();

    AggregateRepository<Aggregate1> aggregate1Repo = new AggregateRepository(eventStore, Aggregate1.class, bus, new EventFactoryBasic(), aggregateFactory);
    AggregateRepository<Aggregate2> aggregate2Repo = new AggregateRepository(eventStore, Aggregate2.class, bus, new EventFactoryBasic(), aggregateFactory);

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
