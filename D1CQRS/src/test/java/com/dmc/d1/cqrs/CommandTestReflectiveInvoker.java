package com.dmc.d1.cqrs;

import com.dmc.d1.cqrs.command.*;
import com.dmc.d1.cqrs.testdomain.Aggregate1;
import com.dmc.d1.cqrs.testdomain.Aggregate2;
import com.dmc.d1.cqrs.testdomain.MyId;
import com.dmc.d1.cqrs.testdomain.command.*;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;


/**
 * Created by davidclelland on 17/05/2016.
 */
public class CommandTestReflectiveInvoker {

    CommandBus bus;

    AggregateEventStore aes = new InMemoryAggregateEventStore();
    AggregateRepository<Aggregate1> repo1 = new AggregateRepository(aes, Aggregate2.class,AnnotatedMethodInvokerStrategy.REFLECTIVE);
    AggregateRepository<Aggregate2> repo2 = new AggregateRepository(aes, Aggregate2.class,AnnotatedMethodInvokerStrategy.REFLECTIVE);

    @Before
    public void setup(){
        List<AbstractCommandHandler<? extends Aggregate>> lst = new ArrayList<>();

        lst.add(new MyCommandHandler1(repo1, AnnotatedMethodInvokerStrategy.REFLECTIVE));
        lst.add(new MyCommandHandler2(repo2, AnnotatedMethodInvokerStrategy.REFLECTIVE));

        bus = new SimpleCommandBus(lst);
    }

    @Test
    public void testCreateAndUpdateCommand(){
        MyId id = new MyId("testId1");

        CreateAggregate1Command command = new CreateAggregate1Command(id, 3 ,5);
        bus.dispatch(command);

        Aggregate1 aggregate = repo1.find(id.toString());

        assertEquals(aggregate.getI1(), 3);
        assertEquals(aggregate.getI2(), 5);

        UpdateAggregate1Command command2 = new UpdateAggregate1Command(id, 6 ,9);
        bus.dispatch(command2);

        aggregate = repo1.find(id.toString());

        assertEquals(aggregate.getI1(), 6);
        assertEquals(aggregate.getI2(), 9);

        //aggregate  committed ->  events should be in AggregateEventStore
        //should be 4 all together 2 for the create and 2 for the update

        assertEquals(4,aes.getAll().size());
    }


    @Test
    public void rollbackTest(){

        MyId id = new MyId("testId2");

        CreateAggregate2Command command = new CreateAggregate2Command(id, "Hello","Goodbye");
        bus.dispatch(command);

        Aggregate2 aggregate = repo2.find(id.toString());

        assertEquals(aggregate.getS1(), "Hello");
        assertEquals(aggregate.getS2(), "Goodbye");

        //command2 handler throws an exception - should rollback
        UpdateAggregate2Command command2 = new UpdateAggregate2Command(id, "Blimey","Where am I");
        bus.dispatch(command2);

        aggregate = repo2.find(id.toString());

        //events which have been stored in the event store get replayed, overwriting the old values
        assertEquals(aggregate.getS1(), "Hello");
        assertEquals(aggregate.getS2(), "Goodbye");
    }
}