package com.dmc.d1.cqrs;


import com.dmc.d1.cqrs.event.store.AggregateEventStore;
import com.dmc.d1.cqrs.event.store.InMemoryAggregateEventStore;
import com.dmc.d1.cqrs.test.aggregate.Aggregate1;
import com.dmc.d1.cqrs.test.aggregate.Aggregate2;
import com.dmc.d1.cqrs.event.SimpleEventBus;
import com.dmc.d1.cqrs.test.domain.MyId;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by davidclelland on 16/05/2016.
 */
public class AggregateTest {

    SimpleEventBus bus = new SimpleEventBus();

    AggregateEventStore eventStore = new InMemoryAggregateEventStore();
    AggregateRepository<Aggregate1> aggregate1Repo = new AggregateRepository(eventStore, Aggregate1.class, bus);
    AggregateRepository<Aggregate2> aggregate2Repo = new AggregateRepository(eventStore, Aggregate2.class, bus);

    MyId id1 = new MyId("testId1");
    MyId id2 = new MyId("testId1");

    @Test
    public void testAggregate() {

        Aggregate1 aggregate1 = new Aggregate1(id1);
        Aggregate2 aggregate2 = new Aggregate2(id2);

        aggregate1Repo.create(aggregate1);
        aggregate2Repo.create(aggregate2);

        aggregate1.doSomething(5, 12);
        aggregate2.doSomething("Hello", "Goodbye");

        assertEquals(5, aggregate1.getI1());
        assertEquals(12, aggregate1.getI2());

        assertEquals("Hello", aggregate2.getS1());
        assertEquals("Goodbye", aggregate2.getS2());
    }
}
