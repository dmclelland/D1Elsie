package com.dmc.d1.cqrs;


import com.dmc.d1.cqrs.event.SimpleEventBus;
import com.dmc.d1.cqrs.sample.aggregate.Aggregate1;
import com.dmc.d1.cqrs.sample.aggregate.Aggregate2;
import com.dmc.d1.cqrs.sample.domain.MyId;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by davidclelland on 16/05/2016.
 */
public class AggregateTest {

    SimpleEventBus bus = new SimpleEventBus();

    AggregateEventStore eventStore = new InMemoryAggregateEventStore();

    AggregateRepository<Aggregate1> aggregate1Repo =
            new AggregateRepository(eventStore, Aggregate1.class, bus, Aggregate1.newInstanceFactory());
    AggregateRepository<Aggregate2> aggregate2Repo =
            new AggregateRepository(eventStore, Aggregate2.class, bus, Aggregate2.newInstanceFactory());

    long id1 = 1;
    long id2 = 2;



    @Test
    public void testAggregate() {

        Aggregate1 aggregate1 = aggregate1Repo.create(id1);
        Aggregate2 aggregate2 = aggregate2Repo.create(id2);

        aggregate1.doSomething(5, 12);
        aggregate2.doSomething("Hello", "Goodbye");

        assertEquals(5, aggregate1.getI1());
        assertEquals(12, aggregate1.getI2());

        assertEquals("Hello", aggregate2.getS1());
        assertEquals("Goodbye", aggregate2.getS2());
    }
}
